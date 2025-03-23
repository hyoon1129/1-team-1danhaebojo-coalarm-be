package _1danhebojo.coalarm.coalarm_service.domain.user.service;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.AlertRepositoryImpl;
import _1danhebojo.coalarm.coalarm_service.domain.alert.service.AlertSSEService;
import _1danhebojo.coalarm.coalarm_service.domain.auth.service.JwtBlacklistService;
import _1danhebojo.coalarm.coalarm_service.domain.user.controller.request.DiscordWebhookRequest;
import _1danhebojo.coalarm.coalarm_service.domain.user.controller.response.DiscordWebhookResponse;
import _1danhebojo.coalarm.coalarm_service.domain.user.controller.response.UserDTO;
import _1danhebojo.coalarm.coalarm_service.domain.user.repository.entity.UserEntity;
import _1danhebojo.coalarm.coalarm_service.domain.user.repository.UserRepository;
import _1danhebojo.coalarm.coalarm_service.domain.user.util.NicknameGenerator;
import _1danhebojo.coalarm.coalarm_service.global.api.ApiException;
import _1danhebojo.coalarm.coalarm_service.global.api.AppHttpStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final RefreshTokenService refreshTokenService;
    private final AlertSSEService alertSSEService;
    private final AlertRepositoryImpl alertRepository;

    @Override
    public UserDTO getMyInfo(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ApiException(AppHttpStatus.UNAUTHORIZED);
        }
        return findByKakaoId(userDetails.getUsername());
    }

    @Override
    @Transactional
    public UserDTO registerOrLogin(String kakaoId, String email) {
        Optional<UserEntity> userOptional = userRepository.findByKakaoId(kakaoId);

        if (userOptional.isPresent()) {
            return UserDTO.fromEntity(userOptional.get());
        }

        String randomNickname = NicknameGenerator.generateNickname(); // 랜덤 닉네임 생성
        UserEntity newUser = UserEntity.builder()
                .kakaoId(kakaoId)
                .nickname(randomNickname)
                .email(email)
                .profileImg(null)
                .build();

        UserEntity savedUser = userRepository.save(newUser);

        alertSSEService.subscribe(savedUser.getUserId());

        return UserDTO.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public void logout(UserDetails userDetails, String authorizationHeader) {
        if (userDetails == null) {
            throw new ApiException(AppHttpStatus.UNAUTHORIZED);
        }

        Long userId = findByKakaoId(userDetails.getUsername()).getUserId();
        refreshTokenService.deleteRefreshToken(userId);

        // TODO : 액세스 토큰 블랙리스트 처리

        alertSSEService.removeEmitter(userId);
    }

    @Override
    public UserDTO findByKakaoId(String kakaoId) {
        UserEntity user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new ApiException(AppHttpStatus.NOT_FOUND));

        return UserDTO.fromEntity(user); // Entity → DTO 변환
    }

    @Override
    public Long updateUser(UserDetails userDetails, String nickname, MultipartFile profileImage) {
        UserEntity user = userRepository.findByKakaoId(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(AppHttpStatus.NOT_FOUND_USER));

        // 닉네임 업데이트
        if (nickname != null && !nickname.isEmpty()) {
            validateNickname(nickname);
            user.updateNickname(nickname);
        }

        // 프로필 이미지 업데이트
        if (profileImage != null && !profileImage.isEmpty()) {
            if (user.getProfileImg() != null) {
                s3Service.deleteImage(user.getProfileImg());
            }
            String imageUrl = s3Service.uploadImage(profileImage);
            user.updateProfileImage(imageUrl);
        }

        userRepository.save(user);
        return user.getUserId();
    }

    private void validateNickname(String nickname) {
        if (nickname.length() < 2) {
            throw new ApiException(AppHttpStatus.BAD_REQUEST);
        }

    }

    @Override
    @Transactional
    public void deleteUser(UserDetails userDetails, String authorizationHeader) {
        if (userDetails == null) {
            throw new ApiException(AppHttpStatus.UNAUTHORIZED);
        }

        UserEntity user = userRepository.findByKakaoId(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(AppHttpStatus.NOT_FOUND_USER));

        // 프로필 이미지 삭제
        if (user.getProfileImg() != null) {
            s3Service.deleteImage(user.getProfileImg());
        }

        // 리프레시 토큰 삭제
        refreshTokenService.deleteRefreshToken(user.getUserId());

        // TODO : 액세스 토큰 블랙리스트 처리

        //유저의 알람 삭제
        alertRepository.deleteByUserId(user.getUserId());

        // 유저 삭제
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public DiscordWebhookResponse updateDiscordWebhook(UserDetails userDetails, DiscordWebhookRequest request) {
        if (userDetails == null) {
            throw new ApiException(AppHttpStatus.UNAUTHORIZED);
        }

        String kakaoId = userDetails.getUsername();

        UserEntity user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new ApiException(AppHttpStatus.NOT_FOUND_USER));

        user.updateDiscordWebhook(request.getDiscordWebhook());

        userRepository.save(user);

        return new DiscordWebhookResponse(user.getUserId());
    }
}