package study.kakao_auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // JSON에 정의되지 않은 속성이 있어도 무시하고 정상적으로 매핑
public class KakaoTokenResponseDto {

    // 역직렬화 목적: 카카오 서버에서 전달받은 JSON 응답을 자바 객체로 변환하기 위해 사용
    // 해당 필드들은 JSON 키와 일치하는 속성명을 @JsonProperty로 지정하여 자동 매핑
    @JsonProperty("token_type")
    public String tokenType;

    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("id_token")
    public String idToken;

    @JsonProperty("expires_in")
    public Integer expiresIn;

    @JsonProperty("refresh_token")
    public String refreshToken;

    @JsonProperty("refresh_token_expires_in")
    public Integer refreshTokenExpiresIn;

    @JsonProperty("scope")
    public String scope;

}
