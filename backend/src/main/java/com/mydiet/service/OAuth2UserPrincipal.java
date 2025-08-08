package com.mydiet.service;

import com.mydiet.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class OAuth2UserPrincipal implements OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    public OAuth2UserPrincipal(User user, Map<String, Object> attributes, String nameAttributeKey) {
        this.user = user;
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        return attributes.get(nameAttributeKey).toString();
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getNickname() {
        return user.getNickname();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public User getUser() {
        return user;
    }

    public String getRole() {
        return user.getRole() != null ? user.getRole() : "USER";
    }

    public Double getWeightGoal() {
        return user.getWeightGoal();
    }

    public String getEmotionMode() {
        return user.getEmotionMode();
    }
}