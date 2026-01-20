package com.cookstemma.cookstemma.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final InternalApiKeyInterceptor internalApiKeyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // /internal/api/ 로 시작하는 모든 요청에 대해 API Key 인터셉터 적용
        registry.addInterceptor(internalApiKeyInterceptor)
                .addPathPatterns("/internal/api/**");
    }

    /**
     * Configure locale resolution from Accept-Language header.
     * Supports: en-US (default), ko-KR, ja-JP, zh-CN.
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.US);
        resolver.setSupportedLocales(List.of(
                Locale.US,
                Locale.KOREA,
                Locale.JAPAN,
                Locale.CHINA
        ));
        return resolver;
    }
}