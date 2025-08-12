package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import com.mydiet.dto.UpdateProfileRequest;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile(HttpSession session) {
        
        log.info("=== 사용자 프로필 조회 요청 ===");
        
        Long userId = (Long) session.getAttribute("userId");
        String userEmail = (String) session.getAttribute("userEmail");
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");
        
        log.info("세션 정보: userId={}, email={}, authenticated={}", userId, userEmail, authenticated);
        
        if (!Boolean.TRUE.equals(authenticated) || userId == null) {
            log.warn("❌ 인증되지 않은 사용자의 프로필 조회 시도");
            return ResponseEntity.status(401)
                .body(Map.of("error", "로그인이 필요합니다.", "needLogin", true));
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                log.error("❌ 사용자를 찾을 수 없음: userId={}", userId);
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
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
            
            log.info("✅ 사용자 프로필 조회 성공: {}, 키: {}, 현재체중: {}", 
                    user.getEmail(), user.getHeight(), user.getCurrentWeight());
            
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            log.error("❌ 사용자 프로필 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
        @RequestBody UpdateProfileRequest request,
        HttpSession session) {
        
        log.info("=== 프로필 업데이트 요청 ===");
        log.info("요청 데이터: {}", request);
        
        Long userId = (Long) session.getAttribute("userId");
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");
        
        if (!Boolean.TRUE.equals(authenticated) || userId == null) {
            log.warn("❌ 인증되지 않은 사용자의 프로필 업데이트 시도");
            return ResponseEntity.status(401)
                .body(Map.of("error", "로그인이 필요합니다.", "needLogin", true));
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                log.error("❌ 사용자를 찾을 수 없음: userId={}", userId);
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            
            if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
                user.setNickname(request.getNickname().trim());
                log.info("닉네임 업데이트: {}", request.getNickname());
            }
            
            if (request.getWeightGoal() != null && request.getWeightGoal() > 0) {
                user.setWeightGoal(request.getWeightGoal());
                log.info("목표 체중 업데이트: {}", request.getWeightGoal());
            }
            
            if (request.getHeight() != null && request.getHeight() > 0) {
                user.setHeight(request.getHeight());
                log.info("키 업데이트: {}", request.getHeight());
            }
            
            if (request.getCurrentWeight() != null && request.getCurrentWeight() > 0) {
                user.setCurrentWeight(request.getCurrentWeight());
                log.info("현재 체중 업데이트: {}", request.getCurrentWeight());
            }
            
            if (request.getEmotionMode() != null && !request.getEmotionMode().trim().isEmpty()) {
                String emotionMode = request.getEmotionMode().trim();
                if (emotionMode.equals("무자비") || emotionMode.equals("츤데레") || emotionMode.equals("다정함") || emotionMode.equals("격려")) {
                    user.setEmotionMode(emotionMode);
                    log.info("감정 모드 업데이트: {}", emotionMode);
                }
            }
            
            user.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(user);
            
            session.setAttribute("userNickname", updatedUser.getNickname());
            
            log.info("✅ 프로필 업데이트 완료: userId={}, email={}, 키={}, 현재체중={}", 
                    userId, updatedUser.getEmail(), updatedUser.getHeight(), updatedUser.getCurrentWeight());
            
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
            log.error("❌ 프로필 업데이트 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "프로필 업데이트에 실패했습니다."));
        }
    }

    @DeleteMapping("/account")
    public ResponseEntity<Map<String, Object>> deleteAccount(HttpSession session) {
        
        log.info("=== 계정 삭제 요청 ===");
        
        Long userId = (Long) session.getAttribute("userId");
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");
        
        if (!Boolean.TRUE.equals(authenticated) || userId == null) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "인증이 필요합니다."));
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            userRepository.deleteById(userId);
            session.invalidate();
            
            log.info("✅ 계정 삭제 완료: userId={}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "계정이 성공적으로 삭제되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("❌ 계정 삭제 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "계정 삭제에 실패했습니다."));
        }
    }
}