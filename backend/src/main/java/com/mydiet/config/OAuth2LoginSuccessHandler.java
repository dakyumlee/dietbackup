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
        
        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
        
        HttpSession session = request.getSession(true);
        
        session.setAttribute("userId", principal.getUser().getId());
        session.setAttribute("userEmail", principal.getUser().getEmail());
        session.setAttribute("userNickname", principal.getUser().getNickname());
        session.setAttribute("authenticated", true);
        session.setAttribute("userRole", principal.getUser().getRole() != null ? 
                           principal.getUser().getRole() : "USER");
        
        session.setMaxInactiveInterval(86400);
        
        log.info("OAuth2 로그인 성공: {} (세션ID: {})", 
                principal.getUser().getEmail(), session.getId());
        log.debug("세션 속성 설정 완료 - authenticated: true, userRole: {}", 
                 session.getAttribute("userRole"));
        
        response.sendRedirect("/dashboard.html");
    }
}