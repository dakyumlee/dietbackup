package com.mydiet.service;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
 
        String email = extractEmail(oauth2User, registrationId);
        String nickname = extractNickname(oauth2User, registrationId);
        String providerId = oauth2User.getAttribute(userNameAttributeName).toString();
 
        User user = userRepository.findByEmail(email)
            .map(existingUser -> updateExistingUser(existingUser, nickname, providerId, registrationId))
            .orElse(createNewUser(email, nickname, providerId, registrationId));
 
        return new OAuth2UserPrincipal(user, oauth2User.getAttributes(), userNameAttributeName);
    }

    private User updateExistingUser(User user, String nickname, String providerId, String provider) {
        user.setNickname(nickname);
        user.setProviderId(providerId);
        user.setProvider(provider);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    private User createNewUser(String email, String nickname, String providerId, String provider) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setNickname(nickname);
        newUser.setProvider(provider);
        newUser.setProviderId(providerId);
        newUser.setRole(User.Role.USER);
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