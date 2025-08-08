package com.mydiet.controller;

import com.mydiet.dto.LoginRequest;
import com.mydiet.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        log.info("테스트 엔드포인트 호출됨");
        return ResponseEntity.ok(Map.of("message", "API 정상 작동"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        log.info("회원가입 시도: {}", request.getEmail());
        return ResponseEntity.ok().body(Map.of("message", "회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.info("로그인 시도: {} / {}", request.getEmail(), request.getPassword());
        
        if ("test@gmail.com".equals(request.getEmail()) && "password".equals(request.getPassword())) {
            
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("userId", 1L);
            session.setAttribute("userEmail", "test@gmail.com");
            session.setAttribute("userNickname", "테스트 사용자");
            session.setAttribute("authenticated", true);
            
            log.info("로그인 성공: test@gmail.com (세션: {})", session.getId());

            return ResponseEntity.ok().body(Map.of(
                "message", "로그인 성공",
                "redirectUrl", "/dashboard.html"
            ));
        }
        
        log.warn("로그인 실패: {}", request.getEmail());
        return ResponseEntity.badRequest().body(
            Map.of("message", "이메일 또는 비밀번호가 올바르지 않습니다.")
        );
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        log.info("현재 사용자 확인 요청");
        
        if (session != null && Boolean.TRUE.equals(session.getAttribute("authenticated"))) {
            log.info("현재 사용자: test@gmail.com");
            return ResponseEntity.ok(Map.of(
                "id", 1L,
                "email", "test@gmail.com",
                "nickname", "테스트 사용자",
                "weightGoal", 70.0,
                "emotionMode", "다정함"
            ));
        }
        
        log.info("인증되지 않은 사용자");
        return ResponseEntity.status(401).body(
            Map.of("message", "로그인이 필요합니다.")
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.info("로그아웃: 세션 무효화");
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }
}