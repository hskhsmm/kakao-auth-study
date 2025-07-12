package study.kakao_auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import study.kakao_auth.dto.KakaoTokenResponseDto;
import study.kakao_auth.dto.KakaoUserInfoResponseDto;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {

    private final WebClient kakaoAuthWebClient;
    private final WebClient kakaoApiWebClient;

    @Value("${kakao.client_id}")
    private String clientId;

    public String getAccessTokenFromKakao(String code) {
        KakaoTokenResponseDto kakaoTokenResponseDto = kakaoAuthWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("code", code)
                        .build(true))
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();

        log.info("[KakaoService] access_token = {}", kakaoTokenResponseDto.getAccessToken());
        log.info("[KakaoService] refresh_token = {}", kakaoTokenResponseDto.getRefreshToken());

        // OpenID Connect를 활성화하면 카카오 로그인 시 사용자 인증 정보가 담긴 ID 토큰을 액세스 토큰과 함께 발급받을 수 있음.
        log.info("[KakaoService] id_token = {}", kakaoTokenResponseDto.getIdToken());
        log.info("[KakaoService] scope = {}", kakaoTokenResponseDto.getScope());

        return Objects.requireNonNull(kakaoTokenResponseDto).getAccessToken();
    }

    public KakaoUserInfoResponseDto getUserInfo(String accessToken) {
        KakaoUserInfoResponseDto userInfo = kakaoApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .block();

        log.info("[KakaoService] Auth ID = {}", userInfo.getId());
        log.info("[KakaoService] NickName = {}", userInfo.getKakaoAccount().getProfile().getNickName());
        log.info("[KakaoService] ProfileImageUrl = {}", userInfo.getKakaoAccount().getProfile().getProfileImageUrl());
        log.info("[KakaoService] Email = {}", userInfo.getKakaoAccount().getEmail());

        return userInfo;
    }


}