package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class FixedProfileController {

    private final UserRepository userRepository;

    private User getFirstUser() {
        List<User> users = userRepository.findAll();
        if (!users.isEmpty()) {
            return users.get(0);
        }
        
        User newUser = new User();
        newUser.setNickname("사용자");
        newUser.setEmail("user@mydiet.com");
        newUser.setRole("USER");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(newUser);
    }

    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getProfile() {
        log.info("프로필 조회 요청");
        
        try {
            User user = getFirstUser();
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("nickname", user.getNickname());
            profile.put("email", user.getEmail());
            profile.put("height", user.getHeight());
            profile.put("currentWeight", user.getCurrentWeight());
            profile.put("weightGoal", user.getWeightGoal());
            profile.put("emotionMode", user.getEmotionMode());
            
            log.info("프로필 조회 성공: {}", user.getNickname());
            return ResponseEntity.ok(profile);
            
        } catch (Exception e) {
            log.error("프로필 조회 실패", e);
            return ResponseEntity.ok(Map.of(
                "nickname", "사용자",
                "email", "user@mydiet.com",
                "emotionMode", "다정함"
            ));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveProfile(@RequestBody Map<String, Object> data) {
        log.info("프로필 저장 요청: {}", data);
        
        try {
            User user = getFirstUser();
            
            if (data.get("nickname") != null) {
                user.setNickname(data.get("nickname").toString());
            }
            
            if (data.get("height") != null) {
                user.setHeight(Double.valueOf(data.get("height").toString()));
            }
            
            if (data.get("currentWeight") != null) {
                user.setCurrentWeight(Double.valueOf(data.get("currentWeight").toString()));
            }
            
            if (data.get("weightGoal") != null) {
                user.setWeightGoal(Double.valueOf(data.get("weightGoal").toString()));
            }
            
            if (data.get("emotionMode") != null) {
                user.setEmotionMode(data.get("emotionMode").toString());
            }
            
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("프로필 저장 성공");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "프로필이 저장되었습니다"
            ));
            
        } catch (Exception e) {
            log.error("프로필 저장 실패", e);
            return ResponseEntity.ok(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}