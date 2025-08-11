package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
@Slf4j
public class TempSessionController {

    private final UserRepository userRepository;

    @GetMapping("/login/user")
    public void loginAsUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("=== 사용자 임시 로그인 시작 ===");
        
        try {
            List<User> users = userRepository.findAll();
            User targetUser = users.stream()
                .filter(u -> "USER".equals(u.getRole()))
                .findFirst()
                .orElse(null);

            if (targetUser == null) {
                targetUser = createTestUser();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", targetUser.getId());
            session.setAttribute("userEmail", targetUser.getEmail());
            session.setAttribute("userNickname", targetUser.getNickname());
            session.setAttribute("userRole", targetUser.getRole());
            session.setAttribute("authenticated", true);

            log.info("사용자 로그인 성공: {} ({})", targetUser.getEmail(), targetUser.getRole());
            
            response.sendRedirect("/dashboard.html");
            
        } catch (Exception e) {
            log.error("사용자 임시 로그인 실패", e);
            response.sendRedirect("/auth.html?error=login_failed");
        }
    }


    @GetMapping("/login/admin")
    public void loginAsAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("=== 관리자 임시 로그인 시작 ===");
        
        try {
            List<User> users = userRepository.findAll();
            User adminUser = users.stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .findFirst()
                .orElse(null);

            if (adminUser == null) {
                adminUser = createTestAdmin();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", adminUser.getId());
            session.setAttribute("userEmail", adminUser.getEmail());
            session.setAttribute("userNickname", adminUser.getNickname());
            session.setAttribute("userRole", adminUser.getRole());
            session.setAttribute("authenticated", true);

            log.info("관리자 로그인 성공: {} ({})", adminUser.getEmail(), adminUser.getRole());
            
            response.sendRedirect("/admin-dashboard.html");
            
        } catch (Exception e) {
            log.error("관리자 임시 로그인 실패", e);
            response.sendRedirect("/auth.html?error=admin_login_failed");
        }
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        log.info("로그아웃 완료");
        response.sendRedirect("/auth.html");
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session != null && Boolean.TRUE.equals(session.getAttribute("authenticated"))) {
            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "userId", session.getAttribute("userId"),
                "email", session.getAttribute("userEmail"),
                "nickname", session.getAttribute("userNickname"),
                "role", session.getAttribute("userRole")
            ));
        }
        
        return ResponseEntity.ok(Map.of("authenticated", false));
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("user@mydiet.com");
        user.setNickname("테스트 사용자");
        user.setRole("USER");
        user.setEmotionMode("다정함");
        user.setWeightGoal(70.0);
        user.setProvider("test");
        user.setProviderId("test_user");
        
        User saved = userRepository.save(user);
        log.info("테스트 사용자 생성: {}", saved.getEmail());
        return saved;
    }

    private User createTestAdmin() {
        User admin = new User();
        admin.setEmail("admin@mydiet.com");
        admin.setNickname("관리자");
        admin.setRole("ADMIN");
        admin.setEmotionMode("무자비");
        admin.setWeightGoal(75.0);
        admin.setProvider("test");
        admin.setProviderId("test_admin");
        
        User saved = userRepository.save(admin);
        log.info("테스트 관리자 생성: {}", saved.getEmail());
        return saved;
    }
}