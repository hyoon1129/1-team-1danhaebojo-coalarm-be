package _1danhebojo.coalarm.coalarm_service.domain.user.controller;

import _1danhebojo.coalarm.coalarm_service.domain.user.controller.response.UserDTO;
import _1danhebojo.coalarm.coalarm_service.domain.user.service.KakaoAuthService;
import _1danhebojo.coalarm.coalarm_service.domain.user.service.UserService;
import _1danhebojo.coalarm.coalarm_service.global.api.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/oauth/kakao")
@RequiredArgsConstructor
public class OAuthController {
    private final KakaoAuthService kakaoAuthService;
    private final UserService userService;

    @GetMapping("/callback")
    public ResponseEntity<BaseResponse<UserDTO>> kakaoCallback(@RequestParam("code") String code) {
        // 카카오 액세스 토큰 요청
        String accessToken = kakaoAuthService.getAccessToken(code);

        // 카카오 사용자 정보 요청
        Map<String, String> userInfo = kakaoAuthService.getUserInfo(accessToken);

        // 유저 정보 저장 또는 로그인 처리
        UserDTO userDTO = userService.registerOrLogin(
                userInfo.get("kakaoId"),
                userInfo.get("email")
        );

        // 클라이언트에게 유저 정보 반환
        return ResponseEntity.ok(BaseResponse.success(userDTO));
    }

    @GetMapping("/userinfo")
    public ResponseEntity<BaseResponse<UserDTO>> getUserInfo(@RequestHeader("Authorization") String accessToken) {
        // 카카오 사용자 정보 요청
        Map<String, String> userInfo = kakaoAuthService.getUserInfo(accessToken);

        // 유저 정보 조회 (새로운 유저가 생성되지는 않음)
        UserDTO userDTO = userService.registerOrLogin(
                userInfo.get("kakaoId"),
                userInfo.get("email")
        );

        return ResponseEntity.ok(BaseResponse.success(userDTO));
    }
}
