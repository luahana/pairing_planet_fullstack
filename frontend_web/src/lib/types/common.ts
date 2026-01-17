/**
 * Image response from API
 */
export interface ImageResponseDto {
  imagePublicId: string;
  imageUrl: string;
}

/**
 * Image upload response
 */
export interface ImageUploadResponse {
  imagePublicId: string;
  imageUrl: string;
}

/**
 * Image type for upload
 */
export type ImageType = 'LOG_POST' | 'COVER' | 'STEP';

/**
 * Hashtag data
 */
export interface HashtagDto {
  publicId: string;
  name: string;
}

/**
 * Hashtag with counts
 */
export interface HashtagCounts {
  exists: boolean;
  normalizedName: string;
  recipeCount: number;
  logPostCount: number;
}
