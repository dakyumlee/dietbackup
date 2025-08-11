package com.mydiet.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminAuthController {

    private static final String ADMIN_PASSWORD = "oicrcutie1998";

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody Map<String, String> request, 
                                                         HttpSession session) {
        log.info("=== 관리자 로그인 시도 ===");
        
        String password = request.get("password");
        
        if (ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("adminAuth", true);
            session.setAttribute("userRole", "ADMIN");
            session.setAttribute("userId", 999L);
            session.setAttribute("userEmail", "admin@mydiet.com");
            session.setAttribute("userNickname", "관리자");
            
            log.info("관리자 로그인 성공");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "관리자 인증 성공",
                "redirectUrl", "/admin-dashboard.html"
            ));
        } else {
            log.warn("관리자 로그인 실패 - 잘못된 비밀번호");
            
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "비밀번호가 올바르지 않습니다"
            ));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAdminAuth(HttpSession session) {
        log.info("=== 관리자 권한 확인 요청 ===");
        
        try {
            Boolean adminAuth = (Boolean) session.getAttribute("adminAuth");
            String userRole = (String) session.getAttribute("userRole");
            
            log.info("세션 adminAuth: {}, userRole: {}", adminAuth, userRole);
            
            if (adminAuth != null && adminAuth && "ADMIN".equals(userRole)) {
                log.info("관리자 권한 확인됨");
                return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "role", "ADMIN",
                    "sessionId", session.getId()
                ));
            } else {
                log.info("관리자 권한 없음");
                return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "sessionId", session.getId()
                ));
            }
        } catch (Exception e) {
            log.error("관리자 권한 확인 중 오류", e);
            return ResponseEntity.status(500).body(Map.of(
                "authenticated", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> adminLogout(HttpSession session) {
        session.removeAttribute("adminAuth");
        session.removeAttribute("userRole");
        session.removeAttribute("userId");
        session.removeAttribute("userEmail");
        session.removeAttribute("userNickname");
        
        log.info("관리자 로그아웃 완료");
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "로그아웃 되었습니다"
        ));
    }
}