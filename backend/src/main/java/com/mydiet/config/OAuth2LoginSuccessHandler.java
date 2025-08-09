package com.mydiet.config;

import com.mydiet.service.OAuth2UserPrincipal;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        log.info("=== OAuth2 로그인 성공 처리 ===");
        
        try {
            if (authentication.getPrincipal() instanceof OAuth2UserPrincipal) {
                OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
                
                HttpSession session = request.getSession(true);
                session.setAttribute("authenticated", true);
                session.setAttribute("userId", principal.getUserId());
                session.setAttribute("userEmail", principal.getEmail());
                session.setAttribute("userNickname", principal.getNickname());
                session.setAttribute("userRole", principal.getRole());
                
                log.info("✅ OAuth 세션 설정 완료: userId={}, email={}", 
                        principal.getUserId(), principal.getEmail());
                
                if ("ADMIN".equals(principal.getRole())) {
                    log.info("🛡️ 관리자 로그인 - admin 대시보드로 리다이렉트");
                    response.sendRedirect("/admin-dashboard.html");
                } else {
                    log.info("👤 일반 사용자 로그인 - 대시보드로 리다이렉트");
                    response.sendRedirect("/dashboard.html");
                }
            } else {
                log.warn("⚠️ 예상하지 못한 Principal 타입: {}", authentication.getPrincipal().getClass());
                response.sendRedirect("/auth.html?error=unexpected_principal");
            }
        } catch (Exception e) {
            log.error("❌ OAuth 로그인 처리 중 오류", e);
            response.sendRedirect("/auth.html?error=oauth_error");
        }
    }
}