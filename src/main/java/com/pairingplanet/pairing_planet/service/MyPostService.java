package com.pairingplanet.pairing_planet.service;

import com.pairingplanet.pairing_planet.domain.entity.post.DailyPost;
import com.pairingplanet.pairing_planet.domain.entity.post.DiscussionPost;
import com.pairingplanet.pairing_planet.domain.entity.post.Post;
import com.pairingplanet.pairing_planet.domain.entity.post.RecipePost;
import com.pairingplanet.pairing_planet.domain.entity.user.User;
import com.pairingplanet.pairing_planet.dto.post.CursorResponse; // [변경]
import com.pairingplanet.pairing_planet.dto.post.PostResponseDto;
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
public class MyPostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    public CursorResponse<PostResponseDto> getMyPosts(UUID userId, String type, String cursor, int size) {
        User user = userRepository.findByPublicId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Class<? extends Post> entityType = getEntityType(type);
        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<Post> slice;

        if (cursor == null || cursor.isBlank()) {
            slice = postRepository.findMyPostsByTypeFirstPage(user.getId(), entityType, pageRequest);
        } else {
            try {
                String[] parts = cursor.split("_");
                Instant cursorTime = Instant.parse(parts[0]);
                if (cursorTime.isBefore(SAFE_MIN_DATE)) cursorTime = SAFE_MIN_DATE;

                UUID cursorPublicId = UUID.fromString(parts[1]);
                Long cursorInternalId = postRepository.findByPublicId(cursorPublicId).map(Post::getId).orElse(0L);
                slice = postRepository.findMyPostsByTypeWithCursor(user.getId(), entityType, cursorTime, cursorInternalId, pageRequest);
            } catch (Exception e) {
                slice = postRepository.findMyPostsByTypeFirstPage(user.getId(), entityType, pageRequest);
            }
        }

        List<PostResponseDto> dtos = slice.getContent().stream()
                .map(post -> PostResponseDto.from(post, urlPrefix))
                .toList();

        String nextCursor = null;
        if (slice.hasNext() && !dtos.isEmpty()) {
            Post lastPost = slice.getContent().get(slice.getContent().size() - 1);
            nextCursor = lastPost.getCreatedAt().toString() + "_" + lastPost.getPublicId();
        }

        long totalCount = postRepository.countMyPostsByType(user.getId(), entityType);

        return new CursorResponse<>(dtos, nextCursor, slice.hasNext(), totalCount);
    }

    private Class<? extends Post> getEntityType(String type) {
        if (type == null) return null;
        return switch (type.toUpperCase()) {
            case "DAILY" -> DailyPost.class;
            case "DISCUSSION" -> DiscussionPost.class;
            case "RECIPE" -> RecipePost.class;
            default -> null;
        };
    }
}