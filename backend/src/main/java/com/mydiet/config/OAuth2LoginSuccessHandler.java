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

@Component
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        try {
            OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
            
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", principal.getUser().getId());
            session.setAttribute("userEmail", principal.getUser().getEmail());
            session.setAttribute("userNickname", principal.getUser().getNickname());
            session.setAttribute("authenticated", true);
            session.setAttribute("userRole", "USER");
            
            session.setMaxInactiveInterval(3600);
            
            log.info("=== OAuth2 로그인 성공 ===");
            log.info("사용자 이메일: {}", principal.getUser().getEmail());
            log.info("사용자 ID: {}", principal.getUser().getId());
            log.info("세션 ID: {}", session.getId());
            log.info("리다이렉트 대상: /dashboard.html");
            
            response.sendRedirect("/dashboard.html");
            
        } catch (Exception e) {
            log.error("OAuth2 로그인 성공 처리 중 오류", e);
            response.sendRedirect("/auth.html?error=oauth_error");
        }
    }
}