package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/user-check")
public class UserCheckController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/db-simple")
    public ResponseEntity<Map<String, Object>> testDbConnection() {
        log.info("=== 실제 DB 연결 테스트 ===");
        
        try {
            long userCount = userRepository.count();
            
            return ResponseEntity.ok(Map.of(
                "dbConnected", true,
                "userCount", userCount,
                "message", "데이터베이스 연결 성공!",
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("DB 연결 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "dbConnected", false,
                "error", e.getMessage(),
                "message", "데이터베이스 연결 실패"
            ));
        }
    }

    @PostMapping("/create-default")
    public ResponseEntity<Map<String, Object>> createDefaultUser() {
        log.info("=== 기본 사용자 생성 ===");
        
        try {
            Optional<User> existing = userRepository.findByEmail("test@example.com");
            if (existing.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "message", "사용자가 이미 존재합니다",
                    "user", existing.get()
                ));
            }

            User user = User.builder()
                .nickname("테스트 사용자")
                .email("test@example.com")
                .weightGoal(70.0)
                .emotionMode("다정함")
                .role(User.Role.USER)
                .build();

            User saved = userRepository.save(user);
            log.info("기본 사용자 생성 완료: ID={}, 닉네임={}", saved.getId(), saved.getNickname());

            return ResponseEntity.ok(Map.of(
                "message", "기본 사용자가 생성되었습니다",
                "user", saved,
                "success", true
            ));

        } catch (Exception e) {
            log.error("기본 사용자 생성 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage(),
                "success", false
            ));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserList() {
        log.info("=== 사용자 목록 조회 ===");
        
        try {
            long count = userRepository.count();
            var users = userRepository.findAll();

            return ResponseEntity.ok(Map.of(
                "userCount", count,
                "users", users,
                "message", "사용자 목록 조회 성공"
            ));

        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}