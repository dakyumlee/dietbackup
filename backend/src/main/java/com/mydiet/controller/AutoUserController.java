package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auto")
@RequiredArgsConstructor
@Slf4j
public class AutoUserController {

    private final UserRepository userRepository;

    @PostConstruct
    public void createDefaultUser() {
        try {
            if (userRepository.count() == 0) {
                User user = new User();
                user.setNickname("테스트사용자");
                user.setEmail("test@test.com");
                user.setWeightGoal(65.0);
                user.setEmotionMode("다정함");
                user.setCreatedAt(LocalDateTime.now());
                user.setRole("USER");
                
                User saved = userRepository.save(user);
                log.info("기본 사용자 자동 생성 완료: ID={}", saved.getId());
            }
        } catch (Exception e) {
            log.error("기본 사용자 생성 실패", e);
        }
    }

    @GetMapping("/login")
    public String autoLogin(HttpSession session) {
        try {
            User user = userRepository.findById(1L).orElse(null);
            if (user == null) {
                createDefaultUser();
                user = userRepository.findById(1L).orElse(null);
            }
            
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userNickname", user.getNickname());
            session.setAttribute("authenticated", true);
            
            log.info("자동 로그인 완료: userId={}", user.getId());
            return "로그인 성공! dashboard.html로 이동하세요";
            
        } catch (Exception e) {
            log.error("자동 로그인 실패", e);
            return "로그인 실패: " + e.getMessage();
        }
    }
}