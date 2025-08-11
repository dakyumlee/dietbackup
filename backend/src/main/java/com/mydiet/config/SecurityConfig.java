package com.mydiet.config;

import com.mydiet.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpSession;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final SessionAuthenticationFilter sessionAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeRequests()
                .antMatchers("/", "/index.html", "/auth.html", 
                    "/api/auth/**", "/css/**", "/js/**", "/images/**").permitAll()
                .antMatchers("/api/test/**", "/api/auth/check", "/api/debug/**").permitAll()
                .antMatchers("/admin/**", "/api/admin/**", "/admin-dashboard.html").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
            .oauth2Login()
                .loginPage("/auth.html")
                .userInfoEndpoint()
                    .userService(oAuth2UserService)
                    .and()
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler((request, response, exception) -> {
                    log.error("OAuth2 로그인 실패", exception);
                    response.sendRedirect("/auth.html?error=oauth_failed");
                })
                .and()
            .formLogin()
                .loginPage("/auth.html")
                .loginProcessingUrl("/api/auth/login")
                .successHandler((request, response, authentication) -> {
                    HttpSession session = request.getSession(true);
                    session.setAttribute("authenticated", true);
                    session.setAttribute("userEmail", authentication.getName());
                    session.setAttribute("userRole", "USER");
                    response.sendRedirect("/dashboard.html");
                })
                .failureHandler((request, response, exception) -> {
                    log.error("폼 로그인 실패", exception);
                    response.sendRedirect("/auth.html?error=login_failed");
                })
                .permitAll()
                .and()
            .logout()
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }
                    response.sendRedirect("/");
                })
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll();

        return http.build();
    }
}