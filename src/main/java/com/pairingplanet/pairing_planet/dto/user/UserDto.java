package com.pairingplanet.pairing_planet.dto.user;

import com.pairingplanet.pairing_planet.domain.entity.user.User;
import com.pairingplanet.pairing_planet.domain.enums.Gender;
import lombok.Builder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record UserDto(
        UUID id,
        String username,
        String profileImageUrl,
        Gender gender,          // [추가] 성별
        LocalDate birthDate    // [추가] 생년월일
) {
    /**
     * User 엔티티를 UserDto로 변환합니다.
     * @param user 변환할 유저 엔티티
     * @param urlPrefix 프로필 이미지 경로 구성을 위한 프리픽스
     */
    public static UserDto from(User user, String urlPrefix) {
        if (user == null) return null;

        String username = user.getUsername();
        String profileUrl = user.getProfileImageUrl();

        // 1. 프로필 이미지 URL 처리
        if (profileUrl != null && !profileUrl.isEmpty()) {
            if (!profileUrl.startsWith("http") && urlPrefix != null) {
                profileUrl = urlPrefix + "/" + profileUrl;
            }
        } else {
            // 이미지가 없을 경우 이니셜 아바타 생성
            String encodedName = URLEncoder.encode(username, StandardCharsets.UTF_8);
            profileUrl = "https://ui-avatars.com/api/?name=" + encodedName + "&background=random&color=fff";
        }

        return UserDto.builder()
                .id(user.getPublicId())
                .username(username)
                .profileImageUrl(profileUrl)
                .gender(user.getGender())       // [매핑] 성별
                .birthDate(user.getBirthDate()) // [매핑] 생년월일
                .build();
    }
}