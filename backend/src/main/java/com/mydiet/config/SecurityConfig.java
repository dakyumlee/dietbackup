package com.mydiet.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .antMatchers("/", "/index.html", "/auth.html", "/dashboard.html", "/dashboard", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                
                .antMatchers("/api/user-check/**").permitAll()
                .antMatchers("/api/test/**").permitAll()
                .antMatchers("/api/dashboard/**").permitAll()
                .antMatchers("/api/data-recording/**").permitAll()
                .antMatchers("/api/integrated-save/**").permitAll()
                .antMatchers("/api/ai/**").permitAll()
                .antMatchers("/api/save/**").permitAll()
                .antMatchers("/api/delete/**").permitAll()
                .antMatchers("/api/user/**").permitAll()
                .antMatchers("/api/claude/**").permitAll()
                
                .antMatchers("/oauth2/**", "/login/**").permitAll()

                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
            );

        return http.build();
    }
}