package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import com.mydiet.service.OAuth2UserPrincipal;
import com.mydiet.dto.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;


    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
        @AuthenticationPrincipal OAuth2UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "인증이 필요합니다."));
        }

        try {
            Long userId = principal.getUserId();
            User user = userRepository.findById(userId).orElse(principal.getUser());
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("email", user.getEmail());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("role", user.getRole());
            userInfo.put("weightGoal", user.getWeightGoal());
            userInfo.put("height", user.getHeight());
            userInfo.put("currentWeight", user.getCurrentWeight());
            userInfo.put("emotionMode", user.getEmotionMode());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("updatedAt", user.getUpdatedAt());
            
            log.debug("사용자 정보 조회: userId={}, email={}", userId, user.getEmail());
            
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "사용자 정보 조회에 실패했습니다."));
        }
    }


    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
        @RequestBody UpdateProfileRequest request,
        @AuthenticationPrincipal OAuth2UserPrincipal principal) {
        
        log.info("=== 프로필 업데이트 요청 ===");
        log.info("요청 데이터: {}", request);
        
        if (principal == null) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "인증이 필요합니다."));
        }

        try {
            Long userId = principal.getUserId();
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            
            if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
                user.setNickname(request.getNickname().trim());
                log.info("닉네임 업데이트: {}", request.getNickname());
            }
            
            if (request.getWeightGoal() != null && request.getWeightGoal() > 0) {
                user.setWeightGoal(request.getWeightGoal());
                log.info("목표 체중 업데이트: {}kg", request.getWeightGoal());
            }
            
            if (request.getEmotionMode() != null && !request.getEmotionMode().trim().isEmpty()) {
                String emotionMode = request.getEmotionMode().trim();
                if (emotionMode.equals("무자비") || emotionMode.equals("츤데레") || emotionMode.equals("다정함") || emotionMode.equals("격려")) {
                    user.setEmotionMode(emotionMode);
                    log.info("감정 모드 업데이트: {}", emotionMode);
                }
            }
            
            if (request.getHeight() != null && request.getHeight() > 0) {
                user.setHeight(request.getHeight());
                log.info("키 업데이트: {}cm", request.getHeight());
            }
            
            if (request.getCurrentWeight() != null && request.getCurrentWeight() > 0) {
                user.setCurrentWeight(request.getCurrentWeight());
                log.info("현재 체중 업데이트: {}kg", request.getCurrentWeight());
            }
            
            user.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(user);
            
            log.info("✅ 사용자 프로필 업데이트 완료: userId={}, email={}", userId, user.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필이 성공적으로 업데이트되었습니다.");
            response.put("user", Map.of(
                "id", updatedUser.getId(),
                "email", updatedUser.getEmail(),
                "nickname", updatedUser.getNickname(),
                "weightGoal", updatedUser.getWeightGoal(),
                "height", updatedUser.getHeight(),
                "currentWeight", updatedUser.getCurrentWeight(),
                "emotionMode", updatedUser.getEmotionMode(),
                "updatedAt", updatedUser.getUpdatedAt()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("프로필 업데이트 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "프로필 업데이트에 실패했습니다."));
        }
    }


    @DeleteMapping("/account")
    public ResponseEntity<Map<String, Object>> deleteAccount(
        @AuthenticationPrincipal OAuth2UserPrincipal principal) {
        
        if (principal == null) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "인증이 필요합니다."));
        }

        try {
            Long userId = principal.getUserId();
            User user = principal.getUser();
            
            if ("ADMIN".equals(user.getRole())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "관리자 계정은 삭제할 수 없습니다."));
            }
            
            userRepository.deleteById(userId);
            
            log.info("사용자 계정 삭제 완료: userId={}, email={}", userId, user.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "계정이 성공적으로 삭제되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("계정 삭제 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "계정 삭제에 실패했습니다."));
        }
    }
}