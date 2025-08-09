package com.mydiet.controller;

import com.mydiet.model.Role;
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

@RestController
@RequestMapping("/api/auto-user")
@RequiredArgsConstructor
@Slf4j
public class AutoUserController {

    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAutoUser() {
        log.info("=== 자동 사용자 생성 ===");

        try {
            User user = new User();
            user.setNickname("자동 생성 사용자");
            user.setEmail("auto@mydiet.com");
            user.setRole(Role.USER);
            user.setProvider("LOCAL");
            user.setWeightGoal(70.0);
            user.setEmotionMode("다정함");
            user.setCreatedAt(LocalDateTime.now());

            User saved = userRepository.save(user);
            log.info("자동 사용자 생성 완료: ID={}, 닉네임={}", saved.getId(), saved.getNickname());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "자동 사용자가 생성되었습니다",
                "user", saved
            ));

        } catch (Exception e) {
            log.error("자동 사용자 생성 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listUsers() {
        log.info("=== 사용자 목록 조회 ===");

        try {
            List<User> users = userRepository.findAll();
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalUsers", users.size());
            result.put("users", users);

            log.info("사용자 목록 조회 완료: 총 {}명", users.size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllUsers() {
        log.info("=== 모든 사용자 삭제 ===");

        try {
            long count = userRepository.count();
            userRepository.deleteAll();
            
            log.info("모든 사용자 삭제 완료: {}명 삭제됨", count);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", count + "명의 사용자가 삭제되었습니다",
                "deletedCount", count
            ));

        } catch (Exception e) {
            log.error("사용자 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}