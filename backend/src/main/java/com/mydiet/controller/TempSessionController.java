package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TempSessionController {
    
    private final UserRepository userRepository;
    
    @PostMapping("/test-admin")
    public ResponseEntity<?> createAdminSession(HttpSession session) {
        try {
            log.info("=== 임시 관리자 세션 생성 ===");
            
            Optional<User> adminOpt = userRepository.findByEmail("admin@mydiet.com");
            User admin;
            
            if (adminOpt.isEmpty()) {
                admin = new User();
                admin.setEmail("admin@mydiet.com");
                admin.setNickname("관리자");
                admin.setRole("ADMIN");
                admin.setWeightGoal(70.0);
                admin.setEmotionMode("다정함");
                admin.setCreatedAt(LocalDateTime.now());
                admin = userRepository.save(admin);
                log.info("새 관리자 계정 생성: {}", admin.getEmail());
            } else {
                admin = adminOpt.get();
                log.info("기존 관리자 계정 사용: {}", admin.getEmail());
            }
            
            session.setAttribute("userId", admin.getId());
            session.setAttribute("userEmail", admin.getEmail());
            session.setAttribute("userNickname", admin.getNickname());
            session.setAttribute("userRole", "ADMIN");
            session.setAttribute("authenticated", true);
            
            log.info("관리자 세션 생성 완료: userId={}, email={}", admin.getId(), admin.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "관리자 세션이 생성되었습니다.",
                "user", Map.of(
                    "id", admin.getId(),
                    "email", admin.getEmail(),
                    "nickname", admin.getNickname(),
                    "role", "ADMIN"
                )
            ));
            
        } catch (Exception e) {
            log.error("관리자 세션 생성 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    

    @PostMapping("/test-user")
    public ResponseEntity<?> createUserSession(HttpSession session) {
        try {
            log.info("=== 임시 사용자 세션 생성 ===");
            
            Optional<User> userOpt = userRepository.findByEmail("test@mydiet.com");
            User user;
            
            if (userOpt.isEmpty()) {
                user = new User();
                user.setEmail("test@mydiet.com");
                user.setNickname("테스트 사용자");
                user.setRole("USER");
                user.setWeightGoal(65.0);
                user.setEmotionMode("다정함");
                user.setCreatedAt(LocalDateTime.now());
                user = userRepository.save(user);
                log.info("새 사용자 계정 생성: {}", user.getEmail());
            } else {
                user = userOpt.get();
                log.info("기존 사용자 계정 사용: {}", user.getEmail());
            }
            
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userNickname", user.getNickname());
            session.setAttribute("userRole", "USER");
            session.setAttribute("authenticated", true);
            
            log.info("사용자 세션 생성 완료: userId={}, email={}", user.getId(), user.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "사용자 세션이 생성되었습니다.",
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "nickname", user.getNickname(),
                    "role", "USER"
                )
            ));
            
        } catch (Exception e) {
            log.error("사용자 세션 생성 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> getSessionStatus(HttpSession session) {
        try {
            Map<String, Object> status = Map.of(
                "sessionId", session.getId(),
                "userId", session.getAttribute("userId"),
                "userEmail", session.getAttribute("userEmail"),
                "userNickname", session.getAttribute("userNickname"),
                "userRole", session.getAttribute("userRole"),
                "authenticated", session.getAttribute("authenticated")
            );
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            session.invalidate();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그아웃되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}