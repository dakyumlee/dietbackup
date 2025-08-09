package com.mydiet.controller;

import com.mydiet.model.Role;
import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/user-check")
@RequiredArgsConstructor
@Slf4j
public class UserCheckController {

    private final UserRepository userRepository;

    @PostMapping("/create-default")
    public ResponseEntity<Map<String, Object>> createDefaultUser() {
        log.info("=== 기본 사용자 생성 ===");
        
        try {
            Optional<User> existing = userRepository.findById(1L);
            if (existing.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "message", "사용자가 이미 존재합니다",
                    "user", existing.get()
                ));
            }

            User user = new User();
            user.setNickname("테스트 사용자");
            user.setEmail("test@example.com");
            user.setWeightGoal(70.0);
            user.setEmotionMode("다정함");
            user.setRole(Role.USER);  // 수정됨
            user.setCreatedAt(LocalDateTime.now());

            User saved = userRepository.save(user);
            log.info("기본 사용자 생성 완료: ID={}, 닉네임={}", saved.getId(), saved.getNickname());

            return ResponseEntity.ok(Map.of(
                "message", "기본 사용자가 생성되었습니다",
                "user", saved
            ));

        } catch (Exception e) {
            log.error("기본 사용자 생성 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage(),
                "details", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserList() {
        log.info("=== 사용자 목록 조회 ===");
        
        try {
            long count = userRepository.count();
            log.info("사용자 총 개수: {}", count);

            if (count == 0) {
                return ResponseEntity.ok(Map.of(
                    "message", "사용자가 없습니다. 기본 사용자를 생성하세요",
                    "userCount", 0,
                    "suggestion", "/api/user-check/create-default 호출"
                ));
            }

            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(Map.of(
                "userCount", count,
                "users", users,
                "message", "사용자 목록 조회 성공"
            ));

        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/get/{userId}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long userId) {
        log.info("=== 사용자 조회: userId={} ===", userId);
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "message", "사용자를 찾을 수 없습니다",
                    "userId", userId,
                    "suggestion", "기본 사용자를 생성하거나 다른 ID를 시도하세요"
                ));
            }

            User user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                "message", "사용자 조회 성공",
                "user", user
            ));

        } catch (Exception e) {
            log.error("사용자 조회 실패: userId={}", userId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage(),
                "userId", userId
            ));
        }
    }

    @GetMapping("/db-simple")
    public ResponseEntity<Map<String, Object>> testDbConnection() {
        log.info("=== 간단한 DB 연결 테스트 ===");
        
        try {
            long userCount = userRepository.count();
            
            return ResponseEntity.ok(Map.of(
                "dbConnected", true,
                "userCount", userCount,
                "timestamp", LocalDateTime.now(),
                "message", "데이터베이스 연결 성공"
            ));

        } catch (Exception e) {
            log.error("DB 연결 테스트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "dbConnected", false,
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName()
            ));
        }
    }
}