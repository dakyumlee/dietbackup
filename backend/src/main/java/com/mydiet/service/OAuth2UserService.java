package com.mydiet.service;

import com.mydiet.model.Role;
import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        final String registrationId = userRequest.getClientRegistration().getRegistrationId();
        final String email = extractEmail(oauth2User, registrationId);
        final String nickname = extractNickname(oauth2User, registrationId);
        final String providerId = getProviderId(oauth2User);
        
        User user = userRepository.findByEmail(email)
            .map(existingUser -> updateExistingUser(existingUser, nickname, providerId, registrationId))
            .orElseGet(() -> createNewUser(email, nickname, providerId, registrationId));
        
        return new OAuth2UserPrincipal(user, oauth2User.getAttributes(), "email");
    }

    private String getProviderId(OAuth2User oauth2User) {
        String providerId = oauth2User.getAttribute("sub");
        if (providerId == null) {
            Object id = oauth2User.getAttribute("id");
            providerId = id != null ? id.toString() : "unknown";
        }
        return providerId;
    }

    private User updateExistingUser(User existingUser, String nickname, String providerId, String provider) {
        existingUser.setNickname(nickname);
        existingUser.setProviderId(providerId);
        existingUser.setProvider(provider);
        
        return userRepository.save(existingUser);
    }

    private User createNewUser(String email, String nickname, String providerId, String provider) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setNickname(nickname);
        newUser.setProvider(provider);
        newUser.setProviderId(providerId);
        newUser.setRole(Role.USER);
        newUser.setEmotionMode("다정함"); 
        newUser.setWeightGoal(70.0);  
        
        return userRepository.save(newUser);
    }

    @SuppressWarnings("unchecked")
    private String extractEmail(OAuth2User oauth2User, String registrationId) {
        if ("google".equals(registrationId)) {
            return oauth2User.getAttribute("email");
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttribute("kakao_account");
            return (String) kakaoAccount.get("email");
        }
        throw new OAuth2AuthenticationException("지원하지 않는 OAuth 제공자입니다.");
    }

    @SuppressWarnings("unchecked")
    private String extractNickname(OAuth2User oauth2User, String registrationId) {
        if ("google".equals(registrationId)) {
            return oauth2User.getAttribute("name");
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> properties = (Map<String, Object>) oauth2User.getAttribute("properties");
            return (String) properties.get("nickname");
        }
        return "사용자";
    }
}