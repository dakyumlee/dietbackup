package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user-check")
@RequiredArgsConstructor
public class UserCheckController {

    private final UserRepository userRepository;

    @PostMapping("/create-test-user")
    public ResponseEntity<Map<String, Object>> createTestUser() {
        try {
            User user = new User();
            user.setNickname("테스트사용자");
            user.setEmail("test@mydiet.com");
            user.setWeightGoal(70.0);
            user.setEmotionMode("다정함");
            user.setRole("USER");
            user.setCreatedAt(LocalDateTime.now());

            User saved = userRepository.save(user);
            log.info("테스트 사용자 생성: {}", saved.getEmail());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", saved.getId(),
                "email", saved.getEmail(),
                "nickname", saved.getNickname()
            ));

        } catch (Exception e) {
            log.error("테스트 사용자 생성 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}