package com.mydiet.controller;

import com.mydiet.service.OAuth2UserPrincipal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class AuthTestController {

    @GetMapping("/auth-status")
    public Map<String, Object> getAuthStatus(@AuthenticationPrincipal OAuth2UserPrincipal principal,
                                           HttpServletRequest request,
                                           HttpSession session) {
        
        Map<String, Object> result = new HashMap<>();
        
        log.info("=== 인증 상태 확인 API 호출 ===");
        
        if (principal != null) {
            result.put("authenticated", true);
            result.put("email", principal.getEmail());
            result.put("nickname", principal.getNickname());
            result.put("role", principal.getRole());
            result.put("userId", principal.getUser().getId());
            log.info("✅ Principal 인증됨: {}", principal.getEmail());
        } else {
            result.put("authenticated", false);
            log.info("❌ Principal 없음");
        }
        
        if (session != null) {
            result.put("sessionExists", true);
            result.put("sessionId", session.getId());
            result.put("sessionUserId", session.getAttribute("userId"));
            result.put("sessionEmail", session.getAttribute("userEmail"));
            result.put("sessionAuthenticated", session.getAttribute("authenticated"));
            log.info("✅ 세션 존재: ID={}, 사용자={}", session.getId(), session.getAttribute("userEmail"));
        } else {
            result.put("sessionExists", false);
            log.info("❌ 세션 없음");
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            result.put("securityContextAuth", true);
            result.put("authName", auth.getName());
            log.info("✅ SecurityContext 인증됨: {}", auth.getName());
        } else {
            result.put("securityContextAuth", false);
            log.info("❌ SecurityContext 인증 안됨");
        }
        
        return result;
    }
    
    @GetMapping("/simple")
    public Map<String, String> simpleTest() {
        log.info("간단한 테스트 API 호출");
        return Map.of("status", "ok", "message", "API 정상 작동");
    }
}