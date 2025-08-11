package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auto")
@RequiredArgsConstructor
public class AutoUserController {

    private final UserRepository userRepository;

    @PostMapping("/create-user")
    public ResponseEntity<Map<String, Object>> createAutoUser() {
        try {
            User user = new User();
            user.setNickname("자동생성사용자");
            user.setEmail("auto@example.com");
            user.setWeightGoal(65.0);
            user.setEmotionMode("다정함");
            user.setRole("USER");
            user.setCreatedAt(LocalDateTime.now());

            User saved = userRepository.save(user);
            log.info("자동 사용자 생성: {}", saved.getEmail());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", saved.getId(),
                "email", saved.getEmail()
            ));

        } catch (Exception e) {
            log.error("자동 사용자 생성 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}