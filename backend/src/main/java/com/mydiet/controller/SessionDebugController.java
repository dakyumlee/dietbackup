package com.mydiet.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/session")
public class SessionDebugController {

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        Map<String, Object> sessionInfo = new HashMap<>();
        
        if (session != null) {
            sessionInfo.put("sessionId", session.getId());
            sessionInfo.put("userId", session.getAttribute("userId"));
            sessionInfo.put("userEmail", session.getAttribute("userEmail"));
            sessionInfo.put("userNickname", session.getAttribute("userNickname"));
            sessionInfo.put("authenticated", session.getAttribute("authenticated"));
            sessionInfo.put("creationTime", session.getCreationTime());
            sessionInfo.put("lastAccessedTime", session.getLastAccessedTime());
        } else {
            sessionInfo.put("message", "세션이 없습니다");
        }
        
        log.info("세션 정보: {}", sessionInfo);
        return ResponseEntity.ok(sessionInfo);
    }
    
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            log.info("세션 초기화: sessionId={}", session.getId());
            session.invalidate();
        }
        
        return ResponseEntity.ok(Map.of("message", "세션이 초기화되었습니다"));
    }
    
    @PostMapping("/force-login/{userId}")
    public ResponseEntity<Map<String, Object>> forceLogin(
            @PathVariable Long userId, 
            HttpServletRequest request) {
        
        HttpSession session = request.getSession(true);
        
        session.setAttribute("userId", userId);
        session.setAttribute("userEmail", "test" + userId + "@example.com");
        session.setAttribute("userNickname", "테스트사용자" + userId);
        session.setAttribute("authenticated", true);
        
        log.info("강제 로그인 설정: userId={}", userId);
        
        return ResponseEntity.ok(Map.of(
            "message", "강제 로그인 설정 완료",
            "userId", userId
        ));
    }
}