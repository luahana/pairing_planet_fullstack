package com.pairingplanet.pairing_planet.service;

import com.pairingplanet.pairing_planet.domain.entity.user.SocialAccount;
import com.pairingplanet.pairing_planet.domain.entity.user.User;
import com.pairingplanet.pairing_planet.domain.enums.AccountStatus;
import com.pairingplanet.pairing_planet.domain.enums.Provider;
import com.pairingplanet.pairing_planet.domain.enums.Role;
import com.pairingplanet.pairing_planet.dto.Auth.AuthResponseDto;
import com.pairingplanet.pairing_planet.dto.Auth.SocialLoginRequestDto;
import com.pairingplanet.pairing_planet.dto.Auth.TokenReissueRequestDto;
import com.pairingplanet.pairing_planet.repository.user.SocialAccountRepository;
import com.pairingplanet.pairing_planet.repository.user.UserRepository;
import com.pairingplanet.pairing_planet.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponseDto socialLogin(SocialLoginRequestDto req) {
        // 1. 소셜 계정 조회 혹은 신규 생성
        Provider providerEnum = req.provider();

        SocialAccount socialAccount = socialAccountRepository
                .findByProviderAndProviderUserId(providerEnum, req.providerUserId())
                .orElseGet(() -> registerNewUser(req));

        User user = socialAccount.getUser();

        // [추가] 로그인 시점에 앱의 시스템 언어로 유저 설정 동기화
        // 사용자가 휴대폰 언어 설정을 바꿨을 경우를 대비해 로그인할 때마다 업데이트합니다.
        if (req.locale() != null && !req.locale().isBlank()) {
            user.setLocale(req.locale());
        }

        // 2. 소셜 토큰 업데이트
        socialAccount.setAccessToken(req.socialAccessToken());
        if (req.socialRefreshToken() != null) {
            socialAccount.setRefreshToken(req.socialRefreshToken());
        }

        // 3. 앱 로그인 처리
        return performLogin(user);
    }

    @Transactional
    public AuthResponseDto reissue(TokenReissueRequestDto req) {
        if (!jwtTokenProvider.validateToken(req.refreshToken())) {
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        UUID publicId = UUID.fromString(jwtTokenProvider.getSubject(req.refreshToken()));

        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getAppRefreshToken() == null || !user.getAppRefreshToken().equals(req.refreshToken())) {
            user.setAppRefreshToken(null);
            throw new IllegalArgumentException("Security Alert: Invalid Token detected. Please login again.");
        }

        return performLogin(user);
    }

    private AuthResponseDto performLogin(User user) {
        // [수정] user.getRole() (Enum) -> .name() (String) 으로 변환하여 전달
        String accessToken = jwtTokenProvider.createAccessToken(user.getPublicId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getPublicId());

        user.setAppRefreshToken(refreshToken);
        user.setLastLoginAt(Instant.now());

        return new AuthResponseDto(accessToken, refreshToken, user.getPublicId(), user.getUsername());
    }

    private SocialAccount registerNewUser(SocialLoginRequestDto req) {
        String username = req.username();
        if (username == null || userRepository.existsByUsername(username)) {
            username = "user_" + UUID.randomUUID().toString().substring(0, 8);
        }

        String initialLocale = (req.locale() != null && !req.locale().isBlank()) ? req.locale() : "ko-KR";

        User user = User.builder()
                .username(username)
                .email(req.email())
                .profileImageUrl(req.profileImageUrl())
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .locale(initialLocale) // 시스템 언어 반영
                .build();
        userRepository.save(user);

        SocialAccount account = SocialAccount.builder()
                .user(user)
                .provider(req.provider())
                .providerUserId(req.providerUserId())
                .build();
        return socialAccountRepository.save(account);
    }
}