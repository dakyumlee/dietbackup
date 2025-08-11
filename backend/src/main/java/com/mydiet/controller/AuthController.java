package com.mydiet.controller;

import com.mydiet.dto.ErrorResponse;
import com.mydiet.dto.LoginRequest;
import com.mydiet.dto.RegisterRequest;
import com.mydiet.model.Role;
import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            log.info("회원가입 요청: email={}, nickname={}", request.getEmail(), request.getNickname());
            
            if (userRepository.findFirstByEmailOrderByIdAsc(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("DUPLICATE_EMAIL", "이미 존재하는 이메일입니다.")
                );
            }
 
            User user = new User();
            user.setNickname(request.getNickname());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setWeightGoal(request.getWeightGoal());
            user.setEmotionMode(request.getEmotionMode());
            user.setRole(Role.USER);
            user.setCreatedAt(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            log.info("회원가입 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

            return ResponseEntity.ok().body(Map.of("message", "회원가입이 완료되었습니다."));

        } catch (Exception e) {
            log.error("회원가입 실패", e);
            return ResponseEntity.status(500).body(
                new ErrorResponse("REGISTER_ERROR", "회원가입 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            log.info("로그인 요청: email={}", request.getEmail());
            
            User user = userRepository.findFirstByEmailOrderByIdAsc(request.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("INVALID_PASSWORD", "비밀번호가 일치하지 않습니다.")
                );
            }

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userNickname", user.getNickname());
            session.setAttribute("authenticated", true);

            log.info("로그인 성공: userId={}, email={}", user.getId(), user.getEmail());

            return ResponseEntity.ok().body(Map.of(
                "message", "로그인 성공",
                "redirectUrl", "/dashboard.html"
            ));

        } catch (Exception e) {
            log.error("로그인 실패: email={}", request.getEmail(), e);
            return ResponseEntity.badRequest().body(
                new ErrorResponse("LOGIN_ERROR", "로그인 실패: " + e.getMessage())
            );
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session != null && session.getAttribute("userId") != null) {
            Long userId = (Long) session.getAttribute("userId");
            Boolean authenticated = (Boolean) session.getAttribute("authenticated");
            
            log.info("현재 사용자 조회: userId={}, authenticated={}", userId, authenticated);
            
            if (Boolean.TRUE.equals(authenticated)) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    return ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "nickname", user.getNickname(),
                        "weightGoal", user.getWeightGoal(),
                        "emotionMode", user.getEmotionMode(),
                        "authenticated", true
                    ));
                }
            }
        }
        
        return ResponseEntity.status(401).body(
            new ErrorResponse("UNAUTHORIZED", "로그인이 필요합니다.")
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.info("로그아웃: userId={}", session.getAttribute("userId"));
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }
}