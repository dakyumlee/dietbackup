package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.service.UserService;
import com.mydiet.service.OAuth2UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAdminAccess(
            @AuthenticationPrincipal OAuth2UserPrincipal principal,
            HttpSession session) {
        
        Map<String, Object> result = new HashMap<>();
        
        if (principal != null && "ADMIN".equals(principal.getRole())) {
            result.put("isAdmin", true);
            result.put("method", "OAuth2");
            result.put("email", principal.getEmail());
            return ResponseEntity.ok(result);
        }
        
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");
        String userRole = (String) session.getAttribute("userRole");
        String userEmail = (String) session.getAttribute("userEmail");
        
        if (Boolean.TRUE.equals(authenticated) && "ADMIN".equals(userRole)) {
            result.put("isAdmin", true);
            result.put("method", "Session");
            result.put("email", userEmail);
            return ResponseEntity.ok(result);
        }
        
        result.put("isAdmin", false);
        result.put("error", "관리자 권한이 필요합니다.");
        return ResponseEntity.status(403).body(result);
    }


    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        try {
            log.info("관리자가 사용자 삭제 요청: userId={}", userId);
            
            userService.deleteUser(userId);
            
            return ResponseEntity.ok(Map.of("message", "사용자가 성공적으로 삭제되었습니다."));
            
        } catch (RuntimeException e) {
            log.error("사용자 삭제 실패: userId={}", userId, e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 삭제 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "사용자 삭제에 실패했습니다."));
        }
    }
}