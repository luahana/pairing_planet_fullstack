package com.pairingplanet.pairing_planet.service;

import com.pairingplanet.pairing_planet.domain.entity.post.SavedPost;
import com.pairingplanet.pairing_planet.domain.entity.post.SavedPost.SavedPostId;
import com.pairingplanet.pairing_planet.domain.entity.post.Post;
import com.pairingplanet.pairing_planet.domain.entity.user.User;
import com.pairingplanet.pairing_planet.dto.post.CursorResponse; // [변경]
import com.pairingplanet.pairing_planet.dto.post.PostResponseDto;
import com.pairingplanet.pairing_planet.repository.post.SavedPostRepository;
import com.pairingplanet.pairing_planet.repository.post.PostRepository;
import com.pairingplanet.pairing_planet.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.pairingplanet.pairing_planet.dto.search.SearchCursorDto.SAFE_MIN_DATE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SavedPostService {

    private final SavedPostRepository savedPostRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @Transactional
    public boolean toggleSave(UUID userPublicId, UUID postPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Post post = postRepository.findByPublicId(postPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        SavedPostId id = new SavedPostId(user.getId(), post.getId());

        if (savedPostRepository.existsById(id)) {
            savedPostRepository.deleteById(id);
            return false;
        } else {
            if (post.isDeleted()) throw new IllegalStateException("Cannot save a deleted post");
            savedPostRepository.save(new SavedPost(user, post));
            return true;
        }
    }

    public CursorResponse<PostResponseDto> getSavedPosts(UUID userPublicId, String cursor, int size) { // [변경]
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Long userId = user.getId();

        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<SavedPost> slice;

        try {
            if (cursor == null || cursor.isBlank()) {
                slice = savedPostRepository.findAllByUserIdFirstPage(userId, pageRequest);
            } else {
                String[] parts = cursor.split("_");
                Instant cursorTime = Instant.parse(parts[0]);
                if (cursorTime.isBefore(SAFE_MIN_DATE)) cursorTime = SAFE_MIN_DATE;

                UUID postPublicId = UUID.fromString(parts[1]);
                Long internalPostId = postRepository.findByPublicId(postPublicId).map(Post::getId).orElse(0L);
                slice = savedPostRepository.findAllByUserIdWithCursor(userId, cursorTime, internalPostId, pageRequest);
            }
        } catch (Exception e) {
            slice = savedPostRepository.findAllByUserIdFirstPage(userId, pageRequest);
        }

        List<PostResponseDto> dtos = slice.getContent().stream()
                .map(sp -> PostResponseDto.from(sp.getPost(), urlPrefix))
                .toList();

        String nextCursor = null;
        if (slice.hasNext() && !slice.getContent().isEmpty()) {
            SavedPost lastItem = slice.getContent().get(slice.getContent().size() - 1);
            nextCursor = lastItem.getCreatedAt().toString() + "_" + lastItem.getPost().getPublicId();
        }

        long totalCount = savedPostRepository.countByUserId(userId);

        // [변경] 통합된 생성자 사용
        return new CursorResponse<>(dtos, nextCursor, slice.hasNext(), totalCount);
    }
}