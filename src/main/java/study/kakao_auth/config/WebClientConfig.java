package study.kakao_auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient kakaoAuthWebClient() {
        return WebClient.builder()
                .baseUrl("https://kauth.kakao.com") // 토큰 발급용
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Bean
    public WebClient kakaoApiWebClient() {
        return WebClient.builder()
                .baseUrl("https://kapi.kakao.com") // 사용자 정보 조회용
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}