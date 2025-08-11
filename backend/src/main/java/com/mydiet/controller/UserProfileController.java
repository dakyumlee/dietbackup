package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserRepository userRepository;

    private Long getCurrentUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                return userId;
            }
        }

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return null;
        }

        User firstUser = users.get(0);
        session.setAttribute("userId", firstUser.getId());
        return firstUser.getId();
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(HttpSession session) {
        log.info("=== 사용자 프로필 조회 ===");

        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자를 찾을 수 없습니다"));
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자 정보를 찾을 수 없습니다"));
            }

            User user = userOpt.get();
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("nickname", user.getNickname());
            profile.put("email", user.getEmail());
            profile.put("weightGoal", user.getWeightGoal());
            profile.put("currentWeight", user.getCurrentWeight() != null ? user.getCurrentWeight() : 70.0);
            profile.put("emotionMode", user.getEmotionMode());
            profile.put("role", user.getRole());

            log.info("프로필 조회 성공: {}", user.getEmail());
            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            log.error("프로필 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, Object> request, 
            HttpSession session) {
        
        log.info("=== 사용자 프로필 업데이트 ===");
        log.info("요청 데이터: {}", request);

        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "사용자를 찾을 수 없습니다"
                ));
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "사용자 정보를 찾을 수 없습니다"
                ));
            }

            User user = userOpt.get();

            if (request.containsKey("nickname")) {
                String nickname = request.get("nickname").toString().trim();
                if (!nickname.isEmpty()) {
                    user.setNickname(nickname);
                    session.setAttribute("userNickname", nickname);
                }
            }

            if (request.containsKey("email")) {
                String email = request.get("email").toString().trim();
                if (!email.isEmpty()) {
                    user.setEmail(email);
                    session.setAttribute("userEmail", email);
                }
            }

            if (request.containsKey("weightGoal")) {
                try {
                    Double weightGoal = Double.valueOf(request.get("weightGoal").toString());
                    if (weightGoal > 0) {
                        user.setWeightGoal(weightGoal);
                    }
                } catch (NumberFormatException e) {
                    log.warn("잘못된 목표 체중 형식: {}", request.get("weightGoal"));
                }
            }

            if (request.containsKey("currentWeight")) {
                try {
                    Double currentWeight = Double.valueOf(request.get("currentWeight").toString());
                    if (currentWeight > 0) {
                        user.setCurrentWeight(currentWeight);
                    }
                } catch (NumberFormatException e) {
                    log.warn("잘못된 현재 체중 형식: {}", request.get("currentWeight"));
                }
            }

            if (request.containsKey("emotionMode")) {
                String emotionMode = request.get("emotionMode").toString();
                user.setEmotionMode(emotionMode);
            }

            User savedUser = userRepository.save(user);
            log.info("프로필 업데이트 성공: {} -> {}", savedUser.getEmail(), savedUser.getNickname());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "프로필이 성공적으로 업데이트되었습니다",
                "user", Map.of(
                    "id", savedUser.getId(),
                    "nickname", savedUser.getNickname(),
                    "email", savedUser.getEmail(),
                    "weightGoal", savedUser.getWeightGoal(),
                    "currentWeight", savedUser.getCurrentWeight(),
                    "emotionMode", savedUser.getEmotionMode()
                )
            ));

        } catch (Exception e) {
            log.error("프로필 업데이트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}