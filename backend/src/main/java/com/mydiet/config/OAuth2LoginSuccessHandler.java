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
            log.info("=== OAuth2 로그인 성공 핸들러 시작 ===");
            
            if (authentication == null || authentication.getPrincipal() == null) {
                log.error("Authentication 또는 Principal이 null입니다");
                response.sendRedirect("/auth.html?error=auth_failed");
                return;
            }
            
            Object principal = authentication.getPrincipal();
            log.info("Principal 타입: {}", principal.getClass().getName());
            
            if (!(principal instanceof OAuth2UserPrincipal)) {
                log.error("예상되지 않은 Principal 타입: {}", principal.getClass().getName());
                response.sendRedirect("/auth.html?error=auth_failed");
                return;
            }
            
            OAuth2UserPrincipal oauth2Principal = (OAuth2UserPrincipal) principal;
            
            if (oauth2Principal.getUser() == null) {
                log.error("OAuth2UserPrincipal의 User가 null입니다");
                response.sendRedirect("/auth.html?error=auth_failed");
                return;
            }
            
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", oauth2Principal.getUser().getId());
            session.setAttribute("userEmail", oauth2Principal.getUser().getEmail());
            session.setAttribute("userNickname", oauth2Principal.getUser().getNickname());
            session.setAttribute("userRole", oauth2Principal.getUser().getRole());
            session.setAttribute("authenticated", true);
            
            log.info("OAuth2 로그인 성공: {} ({})", 
                    oauth2Principal.getUser().getEmail(), 
                    oauth2Principal.getUser().getRole());
            
            if ("ADMIN".equals(oauth2Principal.getUser().getRole())) {
                response.sendRedirect("/admin-dashboard.html");
            } else {
                response.sendRedirect("/dashboard.html");
            }
            
        } catch (Exception e) {
            log.error("OAuth2 로그인 성공 핸들러에서 오류 발생", e);
            response.sendRedirect("/auth.html?error=auth_failed");
        }
    }
}