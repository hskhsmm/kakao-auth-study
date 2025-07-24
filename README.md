![스크린샷 2025-06-23 오후 5 38 21](https://github.com/user-attachments/assets/4caf8b36-85b7-45d8-bfae-fb4bcb7015c2)

> 카카오 공식 문서를 기반으로 로그인 연동 절차를 이해하고 적용하는 실습 프로젝트입니다. <br>
> Spring Boot 3.x 환경에서 카카오 로그인을 적용하기 위해 WebClient를 활용하고, Thymeleaf를 통해 페이지를 구성했습니다.
> **https://developers.kakao.com/product/kakaoLogin**

<br>

## 카카오 로그인 이해하기
![kakaologin_process](https://github.com/user-attachments/assets/679b3a31-9fef-4166-9ac8-7c9f40ad051c)
> 카카오 로그인으로 서비스에 로그인하는 과정

### 0. 애플리케이션 생성
- 먼저 카카오에서 애플리케이션을 추가하고 앱 키를 확인합니다.
- 리다이렉트 url을 등록해줍니다. **ex) http://localhost:8080/callback**
<img width="256" height="333" alt="image" src="https://github.com/user-attachments/assets/da536bcc-60d0-41a0-a121-568b7a9412b0" />
<br><br>
<img width="425" height="252" alt="image" src="https://github.com/user-attachments/assets/49b040c4-e885-4444-a2c6-ef8b3a7f0b26" />
<br><br><br>

- 동의항목에 들어가서 카카오 로그인 과정에서 사용자로부터 받을 정보를 설정합니다.
- 일부 항목만 선택이 가능하고 선택 불가능한 것들은 비즈앱 등록 및 추가 심사를 거쳐야 사용 가능합니다.
<img width="1000" height="314" alt="image" src="https://github.com/user-attachments/assets/8a5b4614-2036-4c3b-aaaa-aaa43174e2b9" />

<br>

### 1. 로그인 시작
- 사용자가 "카카오로 로그인" 버튼을 누르면, `client_id`와 `redirect_uri`가 포함된 인증 요청 URL로 이동합니다.

![스크린샷 2025-06-23 오후 6 27 20](https://github.com/user-attachments/assets/24a8e256-63b3-4db6-a5f9-61735f9c67a1)
```java
@Controller
@RequestMapping("/login")
public class KakaoLoginPageController {

    @Value("${kakao.client_id}")
    private String client_id;

    @Value("${kakao.redirect_uri}")
    private String redirect_uri;

    @GetMapping("/page")
    public String loginPage(Model model) {
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="+client_id+"&redirect_uri="+redirect_uri;
        model.addAttribute("location", location);

        return "login";
    }
}
```
```html
<h1>카카오 로그인</h1>
<a th:href="${location}">
  <img src="/kakao_login_medium_narrow.png" alt="카카오 로그인 버튼">
</a>
```

### 2. 카카오 계정 인증
- 카카오 로그인 페이지로 리디렉션되며, 사용자는 자신의 카카오 계정으로 로그인합니다.
<img width="656" height="590" alt="image" src="https://github.com/user-attachments/assets/efae8131-667c-4fe4-8c4c-c6c3ceeaceb5" />
<img width="742" height="595" alt="image" src="https://github.com/user-attachments/assets/81296f07-0e85-46e6-ac39-c1a72b329191" />





### 3. 인가 코드 수신
- 로그인이 완료되면, 카카오는 미리 설정한 Redirect URI로 사용자 브라우저를 이동시키며 빈 페이지가 나오지만 url에 `code` 파라미터를 함께 전달합니다.  
- 이 `code`는 인증을 증명하는 인가 코드입니다.
```java
@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoLoginController {

    private final KakaoService kakaoService;

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        log.info("카카오로부터 받아온 인가 코드: {}", code);

        String accessToken = kakaoService.getAccessTokenFromKakao(code);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
```

### 4. 토큰 교환
- 서버는 이 인가 코드를 카카오 토큰 발급 엔드포인트에 전달하여, 액세스 토큰과 리프레시 토큰을 발급받습니다.
- `https://kauth.kakao.com/oauth/token` URL로 POST 요청을 보내 토큰 발급 요청 (추후 토큰으로 사용자 정보 요청)
- [카카오 측 응답 파라미터](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-token-response-body)를 참고하여 DTO 클래스 생성 후 매핑 필요

> 

```java
@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {

    private final WebClient kakaoAuthWebClient;

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

}
```
<br>
<img width="643" height="112" alt="image" src="https://github.com/user-attachments/assets/d630e0b0-227a-43b6-a128-f77984eb667c" />


### 5. 사용자 정보 요청
- 발급받은 액세스 토큰을 이용해 카카오 API에 사용자 정보를 요청할 수 있습니다.
- 카카오로부터 제공되는 전체 응답 구조는 [해당 문서](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#kakaoaccount)를 참고
- 현재 서비스에서는 아래 3가지 항목에 대해서만 동의를 받고 있으며, 해당 항목만 DTO에 포함
> 사용자가 동의하지 않은 항목은 null 이기 때문에 동의 여부에 따라 해당 필드가 존재하는지 확인 필요
```java
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfoResponseDto {

    // 이메일, 닉네임, 프로필 이미지

    @JsonProperty("id")
    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {

        @JsonProperty("email")
        private String email;

        @JsonProperty("profile")
        private Profile profile;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {

            @JsonProperty("nickname")
            private String nickname;

            @JsonProperty("profile_image_url")
            private String profileImageUrl;
        }
    }
}
```
```java
public KakaoUserInfoResponseDto getUserInfo(String accessToken) {
    KakaoUserInfoResponseDto userInfo = kakaoApiWebClient.get()
            .uri(uriBuilder -> uriBuilder
                    .scheme("https")
                    .path("/v2/user/me")
                    .build(true))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .retrieve()
            .bodyToMono(KakaoUserInfoResponseDto.class)
            .block();

    log.info("[KakaoService] Auth ID = {}", userInfo.getId());
    log.info("[KakaoService] NickName = {}", userInfo.getKakaoAccount().getProfile().getNickName());
    log.info("[KakaoService] ProfileImageUrl = {}", userInfo.getKakaoAccount().getProfile().getProfileImageUrl());
    log.info("[KakaoService] Email = {}", userInfo.getKakaoAccount().getEmail());

    return userInfo;
}
```

<br>

**Postman API 테스트 결과**
<img width="849" height="326" alt="image" src="https://github.com/user-attachments/assets/785d4e6e-3331-49bf-923f-e945d590db6c" />


> 현재 로그인한 사용자의 정보를 불러오기

- 사용자 정보 요청 성공 시, 응답 본문은 사용자 정보를 포함한 JSON 객체를 반환
- [자세한 응답 본문](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info)

### 6. 서비스 로그인 처리
- 가져온 사용자 정보를 기반으로 기존 회원 여부를 확인하고, 신규라면 회원가입, 기존 유저라면 로그인 처리를 합니다.

> getUserInfo 메서드를 통해 카카오에서 받은 사용자 정보를 서버에서 받아왔으면, 이 정보를 활용해 실제 회원가입 또는 로그인 로직을 구현 (최종)

**1. 사용자 정보 파싱**
- 카카오에서 받은 JSON 응답을 DTO로 변환해 필요한 정보를 추출
  
**2. 회원 데이터베이스 조회**
- 추출한 kakaoId 또는 email을 기준으로 기존 회원인지 확인
- 이미 가입된 회원이면 로그인 처리(세션 생성 또는 JWT 발급 등)
  
**3. 신규 회원가입 처리**
- 해당 사용자가 처음이라면, 회원 테이블에 정보를 저장하고 회원가입 절차를 마침
  
**4. 로그인 완료 후 서비스 이용**
- 로그인 성공 후 토큰을 발급하거나 세션에 사용자 정보를 저장해 서비스 접근을 허용합니다.

> 예시 코드 스니펫
```java
public String processKakaoLogin(KakaoUserInfoResponseDto userInfo) {
    Long kakaoId = userInfo.getId();
    String email = userInfo.getKakaoAccount().getEmail();
    String nickname = userInfo.getKakaoAccount().getProfile().getNickname();
    String profileImageUrl = userInfo.getKakaoAccount().getProfile().getProfileImageUrl();

    // 1. DB에서 카카오 ID로 사용자 조회
    Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);

    User user;
    if (existingUser.isPresent()) {
        user = existingUser.get();
        // 2. 기존 회원 로그인 처리
    } else {
        // 3. 신규 회원 가입
        user = User.builder()
                .kakaoId(kakaoId)
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
        userRepository.save(user);
    }

    // 4. 로그인 토큰 발급 및 반환
    return jwtTokenProvider.createToken(user.getUsername(), user.getRoles());
}
```
