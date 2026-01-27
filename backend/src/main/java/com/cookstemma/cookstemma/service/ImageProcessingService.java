package com.cookstemma.cookstemma.service;

import com.cookstemma.cookstemma.domain.entity.image.Image;
import com.cookstemma.cookstemma.domain.enums.ImageStatus;
import com.cookstemma.cookstemma.domain.enums.ImageVariant;
import com.cookstemma.cookstemma.repository.image.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import dev.matrixlab.webp4j.WebPCodec;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    private final S3Client s3Client;
    private final ImageRepository imageRepository;

    @Value("${file.upload.bucket}")
    private String bucket;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    // Track if WebP fallback warning has been logged (to avoid log spam)
    private volatile boolean webpFallbackWarningLogged = false;

    /**
     * Result of image encoding, containing the bytes and format used.
     */
    private record EncodingResult(byte[] data, String format, String contentType, String extension) {}

    @Async("imageProcessingExecutor")
    @Transactional
    public void generateVariantsAsync(Long originalImageId) {
        // Add a small delay to ensure the calling transaction has committed
        // This prevents race condition where we read recipe_id as NULL
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        generateVariantsSync(originalImageId);
    }

    /**
     * Synchronous variant generation for debugging.
     * Call this directly to test if the processing logic works.
     */
    @Transactional
    public void generateVariantsSync(Long originalImageId) {
        generateVariantsSyncWithResult(originalImageId);
    }

    /**
     * Synchronous variant generation with detailed result for debugging.
     */
    @Transactional
    public String generateVariantsSyncWithResult(Long originalImageId) {
        StringBuilder result = new StringBuilder();
        result.append("Processing image ID: ").append(originalImageId).append("\n");

        try {
            Image original = imageRepository.findById(originalImageId)
                    .orElseThrow(() -> new IllegalArgumentException("Image not found: " + originalImageId));

            result.append("Found image: ").append(original.getStoredFilename()).append("\n");

            // Skip if already has variants or is a variant itself
            if (original.getOriginalImage() != null || original.hasVariants()) {
                result.append("SKIPPED: Image already has variants or is a variant itself\n");
                return result.toString();
            }

            // Download original from S3
            result.append("Downloading from S3...\n");
            byte[] originalBytes = downloadFromS3(original.getStoredFilename());
            result.append("Downloaded ").append(originalBytes.length).append(" bytes\n");

            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));

            if (originalImage == null) {
                result.append("ERROR: Failed to read image as BufferedImage\n");
                return result.toString();
            }

            result.append("Image dimensions: ").append(originalImage.getWidth())
                  .append("x").append(originalImage.getHeight()).append("\n");

            // Update original with metadata
            original.setVariantType(ImageVariant.ORIGINAL);
            original.setWidth(originalImage.getWidth());
            original.setHeight(originalImage.getHeight());
            original.setFileSize((long) originalBytes.length);
            original.setFormat(getFormatFromFilename(original.getStoredFilename()));

            int generated = 0;
            int skipped = 0;

            // Generate each variant
            for (ImageVariant variant : ImageVariant.values()) {
                if (variant == ImageVariant.ORIGINAL) continue;

                // Skip large variants for profile images (only need small thumbnails)
                if (original.getType() != null && !original.getType().needsLargeVariants()) {
                    if (variant == ImageVariant.LARGE_1200 || variant == ImageVariant.MEDIUM_800) {
                        result.append("  ").append(variant).append(": SKIPPED (profile image)\n");
                        skipped++;
                        continue;
                    }
                }

                try {
                    boolean created = generateVariantWithResult(original, originalImage, variant, result);
                    if (created) generated++;
                    else skipped++;
                } catch (Exception e) {
                    result.append("ERROR generating ").append(variant).append(": ").append(e.getMessage()).append("\n");
                }
            }

            imageRepository.save(original);
            result.append("\nSUMMARY: Generated ").append(generated).append(" variants, skipped ").append(skipped).append("\n");
            result.append("Variants in DB: ").append(original.getVariants().size()).append("\n");

        } catch (Exception e) {
            result.append("FATAL ERROR: ").append(e.getMessage()).append("\n");
        }

        return result.toString();
    }

    private boolean generateVariantWithResult(Image original, BufferedImage sourceImage, ImageVariant variant, StringBuilder result) throws IOException {
        int originalWidth = sourceImage.getWidth();
        int originalHeight = sourceImage.getHeight();
        int maxDim = variant.getMaxDimension();

        // Skip if image is smaller than target
        if (originalWidth <= maxDim && originalHeight <= maxDim) {
            result.append("  ").append(variant).append(": SKIPPED (").append(originalWidth).append(" <= ").append(maxDim).append(")\n");
            return false;
        }

        // Calculate new dimensions maintaining aspect ratio
        double scale = Math.min((double) maxDim / originalWidth, (double) maxDim / originalHeight);
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        // Resize image using Thumbnailator
        BufferedImage resizedImage = Thumbnails.of(sourceImage)
                .size(newWidth, newHeight)
                .asBufferedImage();

        // Encode to WebP format (with JPEG fallback if native library unavailable)
        EncodingResult encoded = encodeToWebP(resizedImage, variant.getQuality());

        // Generate new filename with appropriate extension
        String originalKey = original.getStoredFilename();
        String baseName = originalKey.substring(originalKey.lastIndexOf('/') + 1);
        String nameWithoutExt = baseName.contains(".") ? baseName.substring(0, baseName.lastIndexOf('.')) : baseName;
        String newKey = variant.getPathPrefix() + "/" + nameWithoutExt + "_" + variant.name().toLowerCase() + encoded.extension();

        // Upload to S3 with appropriate content type
        uploadToS3(newKey, encoded.data(), encoded.contentType());

        // Save variant record
        Image variantImage = Image.builder()
                .storedFilename(newKey)
                .originalFilename(original.getOriginalFilename())
                .status(ImageStatus.ACTIVE)
                .type(original.getType())
                .displayOrder(original.getDisplayOrder())
                .uploaderId(original.getUploaderId())
                .variantType(variant)
                .originalImage(original)
                .width(newWidth)
                .height(newHeight)
                .fileSize((long) encoded.data().length)
                .format(encoded.format())
                .build();

        original.getVariants().add(variantImage);
        result.append("  ").append(variant).append(": CREATED ").append(newWidth).append("x").append(newHeight)
              .append(" (").append(encoded.data().length).append(" bytes) -> ").append(newKey).append("\n");
        return true;
    }

    private void generateVariant(Image original, BufferedImage sourceImage, ImageVariant variant) throws IOException {
        int originalWidth = sourceImage.getWidth();
        int originalHeight = sourceImage.getHeight();
        int maxDim = variant.getMaxDimension();

        // Skip if image is smaller than target
        if (originalWidth <= maxDim && originalHeight <= maxDim) {
            log.debug("Skipping {} variant: original is smaller", variant);
            return;
        }

        // Calculate new dimensions maintaining aspect ratio
        double scale = Math.min((double) maxDim / originalWidth, (double) maxDim / originalHeight);
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        // Resize image using Thumbnailator
        BufferedImage resizedImage = Thumbnails.of(sourceImage)
                .size(newWidth, newHeight)
                .asBufferedImage();

        // Encode to WebP format (with JPEG fallback if native library unavailable)
        EncodingResult encoded = encodeToWebP(resizedImage, variant.getQuality());

        // Generate new filename with appropriate extension
        String originalKey = original.getStoredFilename();
        String baseName = originalKey.substring(originalKey.lastIndexOf('/') + 1);
        String nameWithoutExt = baseName.contains(".") ? baseName.substring(0, baseName.lastIndexOf('.')) : baseName;
        String newKey = variant.getPathPrefix() + "/" + nameWithoutExt + "_" + variant.name().toLowerCase() + encoded.extension();

        // Upload to S3 with appropriate content type
        uploadToS3(newKey, encoded.data(), encoded.contentType());

        // Save variant record
        Image variantImage = Image.builder()
                .storedFilename(newKey)
                .originalFilename(original.getOriginalFilename())
                .status(ImageStatus.ACTIVE)
                .type(original.getType())
                .displayOrder(original.getDisplayOrder())
                .uploaderId(original.getUploaderId())
                .variantType(variant)
                .originalImage(original)
                .width(newWidth)
                .height(newHeight)
                .fileSize((long) encoded.data().length)
                .format(encoded.format())
                .build();

        original.getVariants().add(variantImage);
        log.debug("Generated {} variant: {}x{}, {} bytes", variant, newWidth, newHeight, encoded.data().length);
    }

    private EncodingResult encodeToWebP(BufferedImage image, int quality) throws IOException {
        try {
            byte[] data = WebPCodec.encodeImage(image, (float) quality);
            return new EncodingResult(data, "webp", "image/webp", ".webp");
        } catch (UnsatisfiedLinkError e) {
            if (!webpFallbackWarningLogged) {
                log.warn("WebP native library unavailable, falling back to JPEG: {}", e.getMessage());
                webpFallbackWarningLogged = true;
            }
            return encodeToJpeg(image, quality);
        }
    }


    /**
     * Fallback encoding to JPEG when WebP native library is unavailable.
     */
    private EncodingResult encodeToJpeg(BufferedImage image, int quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer available");
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality / 100.0f);

            // Handle transparency - convert to RGB if needed
            BufferedImage rgbImage = image;
            if (image.getColorModel().hasAlpha()) {
                rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = rgbImage.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, image.getWidth(), image.getHeight());
                g.drawImage(image, 0, 0, null);
                g.dispose();
            }

            writer.write(null, new IIOImage(rgbImage, null, null), param);
        } finally {
            writer.dispose();
        }

        return new EncodingResult(baos.toByteArray(), "jpeg", "image/jpeg", ".jpg");
    }

    private byte[] downloadFromS3(String key) throws IOException {
        try (InputStream is = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build())) {
            return is.readAllBytes();
        }
    }

    private void uploadToS3(String key, byte[] data, String contentType) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .cacheControl("public, max-age=31536000") // 1 year cache
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(data));
    }

    private String getFormatFromFilename(String filename) {
        if (filename == null || !filename.contains(".")) return "unknown";
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> "jpeg";
            case "png" -> "png";
            case "gif" -> "gif";
            case "webp" -> "webp";
            default -> ext;
        };
    }

    public String getVariantUrl(Image image, ImageVariant variant) {
        if (image == null) return null;

        // If requesting original or no variants exist, return original
        if (variant == ImageVariant.ORIGINAL || !image.hasVariants()) {
            return urlPrefix + "/" + image.getStoredFilename();
        }

        // Find the requested variant
        return image.getVariants().stream()
                .filter(v -> v.getVariantType() == variant)
                .findFirst()
                .map(v -> urlPrefix + "/" + v.getStoredFilename())
                .orElse(urlPrefix + "/" + image.getStoredFilename());
    }
}
