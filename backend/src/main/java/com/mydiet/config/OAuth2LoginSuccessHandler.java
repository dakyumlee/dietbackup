package com.mydiet.config;

import com.mydiet.service.OAuth2UserPrincipal;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        log.info("=== OAuth2 로그인 성공 핸들러 시작 ===");
        
        try {
            OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
            
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", principal.getUser().getId());
            session.setAttribute("userEmail", principal.getUser().getEmail());
            session.setAttribute("userNickname", principal.getUser().getNickname());
            session.setAttribute("authenticated", true);
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.info("✅ OAuth2 로그인 성공!");
            log.info("사용자 ID: {}", principal.getUser().getId());
            log.info("사용자 이메일: {}", principal.getUser().getEmail());
            log.info("세션 ID: {}", session.getId());
            
            String targetUrl = "/dashboard.html";
            log.info("리다이렉트 URL: {}", targetUrl);
            
            response.sendRedirect(targetUrl);
            
        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 오류 발생", e);
            response.sendRedirect("/auth.html?error=oauth_error");
        }
    }
}