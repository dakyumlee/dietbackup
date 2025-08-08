package com.mydiet.config;

import com.mydiet.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/", "/index.html", "/auth.html", "/ai-test.html", 
                    "/dashboard.html", "/profile-settings.html", "/meal-management.html",
                    "/workout-management.html", "/emotion-diary.html", "/admin-login.html",
                    "/admin-dashboard.html").permitAll()
                .antMatchers("/api/auth/**", "/api/simple/**", "/api/simple-ai/**", 
                    "/api/test/**", "/api/ai/**", "/health", "/test-ai").permitAll()
                .antMatchers("/css/**", "/js/**", "/images/**", "/**/*.css", 
                    "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.gif").permitAll()
                .antMatchers("/admin/**", "/api/admin/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .oauth2Login()
                .loginPage("/auth.html")
                .userInfoEndpoint()
                    .userService(oAuth2UserService)
                    .and()
                .successHandler(oAuth2LoginSuccessHandler)
                .failureUrl("/auth.html?error=true")
                .and()
            .formLogin()
                .loginPage("/auth.html")
                .loginProcessingUrl("/api/auth/login")
                .successHandler((request, response, authentication) -> {
                    response.sendRedirect("/dashboard.html");
                })
                .failureHandler((request, response, exception) -> {
                    response.sendRedirect("/auth.html?error=true");
                })
                .permitAll()
                .and()
            .logout()
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll();

        return http.build();
    }
}