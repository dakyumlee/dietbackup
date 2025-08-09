package com.mydiet.controller;

import com.mydiet.model.Role;
import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String nickname = request.get("nickname");
            
            log.info("회원가입 요청: {}", email);
            
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "이미 존재하는 이메일입니다."));
            }
            
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setNickname(nickname);
            newUser.setRole(Role.USER);
            newUser.setProvider("LOCAL");
            newUser.setEmotionMode("다정함");
            newUser.setWeightGoal(70.0);
            newUser.setCreatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(newUser);
            
            log.info("✅ 회원가입 성공: {}", savedUser.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "회원가입이 완료되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("❌ 회원가입 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "회원가입 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/login-form")
    public ResponseEntity<Map<String, Object>> loginForm(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            log.info("폼 로그인 요청: {}", email);
            
            User user = userRepository.findByEmail(email)
                .orElse(null);
                
            if (user == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "존재하지 않는 이메일입니다."));
            }
            
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "비밀번호가 일치하지 않습니다."));
            }
            
            session.setAttribute("authenticated", true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userNickname", user.getNickname());
            session.setAttribute("userRole", user.getRoleString());
            
            log.info("✅ 폼 로그인 성공: userId={}, email={}", user.getId(), user.getEmail());
            
            String redirectUrl = Role.ADMIN.equals(user.getRole()) ? 
                "/admin-dashboard.html" : "/dashboard.html";
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "redirectUrl", redirectUrl,
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "nickname", user.getNickname(),
                    "role", user.getRoleString()
                )
            ));
            
        } catch (Exception e) {
            log.error("❌ 폼 로그인 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "로그인 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        try {
            if (session != null) {
                log.info("로그아웃 요청: sessionId={}", session.getId());
                session.invalidate();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그아웃 되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("❌ 로그아웃 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "로그아웃 중 오류가 발생했습니다."));
        }
    }
}