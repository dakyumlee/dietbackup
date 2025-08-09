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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
        return http
            .csrf().disable()
            .headers().frameOptions().deny()
            .and()
            
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            .authorizeRequests()
                .antMatchers("/", "/index.html", "/auth.html", "/admin-login.html",
                    "/api/auth/**", "/css/**", "/js/**", "/images/**", 
                    "/api/test/**", "/api/debug/**", "/api/auto-user/**", "/api/user-check/**").permitAll()
                .antMatchers("/admin/**", "/api/admin/**").permitAll()
                .anyRequest().authenticated()
                .and()
            
            .oauth2Login()
                .loginPage("/auth.html")
                .userInfoEndpoint()
                    .userService(oAuth2UserService)
                    .and()
                .successHandler(oAuth2LoginSuccessHandler)
                .failureUrl("/auth.html?error=oauth_failed")
                .and()
            
            .formLogin()
                .loginPage("/auth.html")
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    request.getSession().setAttribute("authenticated", true);
                    request.getSession().setAttribute("userEmail", authentication.getName());
                    response.sendRedirect("/dashboard.html");
                })
                .failureHandler((request, response, exception) -> {
                    response.sendRedirect("/auth.html?error=login_failed");
                })
                .permitAll()
                .and()
            
            .logout()
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            
            .and()
            .build();
    }
}