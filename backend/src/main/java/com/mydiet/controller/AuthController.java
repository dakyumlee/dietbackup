package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.service.UserService;
import com.mydiet.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request, HttpSession session) {
        String email = request.get("email");
        String password = request.get("password");
        
        log.info("로그인 시도: email={}", email);
        
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            
            String token = jwtUtil.generateToken(user.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그인 성공",
                "token", token,
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "nickname", user.getNickname()
                )
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "사용자를 찾을 수 없습니다."
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request, HttpSession session) {
        String email = request.get("email");
        String nickname = request.get("nickname");
        String password = request.get("password");
        
        log.info("회원가입 시도: email={}, nickname={}", email, nickname);
        
        if (userService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "이미 존재하는 이메일입니다."
            ));
        }
        
        User user = userService.createUser(email, nickname, "local", null);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        
        String token = jwtUtil.generateToken(user.getEmail());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "회원가입 성공",
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "nickname", user.getNickname()
            )
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "로그아웃 성공"
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "로그인이 필요합니다."
            ));
        }
        
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "nickname", user.getNickname(),
                    "weightGoal", user.getWeightGoal(),
                    "emotionMode", user.getEmotionMode()
                )
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "사용자를 찾을 수 없습니다."
            ));
        }
    }
}