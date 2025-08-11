package com.mydiet.controller;

import com.mydiet.dto.ErrorResponse;
import com.mydiet.dto.LoginRequest;
import com.mydiet.dto.RegisterRequest;
import com.mydiet.model.Role;
import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("이미 존재하는 이메일입니다.")
                );
            }
 
            User user = User.builder()
                .nickname(request.getNickname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .weightGoal(request.getWeightGoal())
                .emotionMode(request.getEmotionMode())
                .createdAt(LocalDateTime.now())
                .role(Role.USER)
                .build();

            userRepository.save(user);

            return ResponseEntity.ok().body(Map.of("message", "회원가입이 완료되었습니다."));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ErrorResponse("회원가입 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("비밀번호가 일치하지 않습니다.")
                );
            }

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userNickname", user.getNickname());
            session.setAttribute("authenticated", true);
            session.setAttribute("userRole", user.getRole().toString());
            session.setMaxInactiveInterval(86400);

            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "로그인 성공",
                "redirectUrl", "/dashboard.html",
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "nickname", user.getNickname(),
                    "role", user.getRole().toString()
                )
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ErrorResponse("로그인 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return ResponseEntity.ok().body(Map.of("message", "로그아웃되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new ErrorResponse("로그아웃 중 오류가 발생했습니다.")
            );
        }
    }
}