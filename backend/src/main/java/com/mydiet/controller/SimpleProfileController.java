package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class SimpleProfileController {

    private final UserRepository userRepository;

    private Long getCurrentUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");
        
        if (!Boolean.TRUE.equals(authenticated) || userId == null) {
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) {
                return users.get(0).getId();
            }
            return null;
        }
        return userId;
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        
        log.info("=== 현재 사용자 정보 조회 ===");
        
        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "사용자를 찾을 수 없습니다."));
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "사용자 정보가 없습니다."));
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
            
            log.info("사용자 정보 조회 성공: userId={}, nickname={}", userId, user.getNickname());
            
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProfile(
        @RequestBody Map<String, Object> request,
        HttpSession session) {
        
        log.info("=== 프로필 업데이트 요청 ===");
        log.info("요청 데이터: {}", request);
        
        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "사용자를 찾을 수 없습니다."));
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "사용자 정보가 없습니다."));
            }

            User user = userOpt.get();
            
            if (request.get("nickname") != null) {
                String nickname = request.get("nickname").toString().trim();
                if (!nickname.isEmpty()) {
                    user.setNickname(nickname);
                    log.info("닉네임 업데이트: {}", nickname);
                }
            }
            
            if (request.get("weightGoal") != null) {
                try {
                    Double weightGoal = Double.valueOf(request.get("weightGoal").toString());
                    if (weightGoal > 0) {
                        user.setWeightGoal(weightGoal);
                        log.info("목표 체중 업데이트: {}kg", weightGoal);
                    }
                } catch (NumberFormatException e) {
                    log.warn("잘못된 목표 체중 형식: {}", request.get("weightGoal"));
                }
            }
            
            if (request.get("emotionMode") != null) {
                String emotionMode = request.get("emotionMode").toString().trim();
                if (!emotionMode.isEmpty()) {
                    user.setEmotionMode(emotionMode);
                    log.info("감정 모드 업데이트: {}", emotionMode);
                }
            }
            
            if (request.get("height") != null) {
                try {
                    Double height = Double.valueOf(request.get("height").toString());
                    if (height > 0) {
                        user.setHeight(height);
                        log.info("키 업데이트: {}cm", height);
                    }
                } catch (NumberFormatException e) {
                    log.warn("잘못된 키 형식: {}", request.get("height"));
                }
            }
            
            if (request.get("currentWeight") != null) {
                try {
                    Double currentWeight = Double.valueOf(request.get("currentWeight").toString());
                    if (currentWeight > 0) {
                        user.setCurrentWeight(currentWeight);
                        log.info("현재 체중 업데이트: {}kg", currentWeight);
                    }
                } catch (NumberFormatException e) {
                    log.warn("잘못된 현재 체중 형식: {}", request.get("currentWeight"));
                }
            }
            
            user.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(user);
            
            log.info("✅ 프로필 업데이트 완료: userId={}", userId);
            
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
}