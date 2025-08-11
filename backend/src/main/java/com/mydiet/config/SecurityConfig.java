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
                .antMatchers("/", "/index.html", "/auth.html", 
                    "/admin-login.html", "/admin-dashboard.html",
                    "/api/auth/**", "/api/admin/**", "/api/debug/**",
                    "/css/**", "/js/**", "/images/**").permitAll()
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