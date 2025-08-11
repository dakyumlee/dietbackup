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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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
 
        try {
            String email = extractEmail(oauth2User, registrationId);
            String nickname = extractNickname(oauth2User, registrationId);
            String providerId = oauth2User.getAttribute(userNameAttributeName).toString();
            
            log.info("OAuth2 로그인 시도: email={}, provider={}", email, registrationId);
     
            User user = userRepository.findByEmail(email)
                .map(existingUser -> updateExistingUser(existingUser, nickname, providerId, registrationId))
                .orElse(createNewUser(email, nickname, providerId, registrationId));
     
            return new OAuth2UserPrincipal(user, oauth2User.getAttributes(), userNameAttributeName);
        } catch (Exception e) {
            log.error("OAuth2 사용자 로드 실패", e);
            throw new OAuth2AuthenticationException("OAuth2 사용자 로드 실패: " + e.getMessage());
        }
    }

    private User updateExistingUser(User user, String nickname, String providerId, String provider) {
        user.setNickname(nickname);
        user.setProviderId(providerId);
        user.setProvider(provider);
        user.setUpdatedAt(LocalDateTime.now());
        
        log.info("기존 사용자 업데이트: userId={}, email={}", user.getId(), user.getEmail());
        return userRepository.save(user);
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
        newUser.setCreatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(newUser);
        log.info("새 OAuth2 사용자 생성: userId={}, email={}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    @SuppressWarnings("unchecked")
    private String extractEmail(OAuth2User oauth2User, String registrationId) {
        if ("google".equals(registrationId)) {
            return oauth2User.getAttribute("email");
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttribute("kakao_account");
            if (kakaoAccount != null) {
                return (String) kakaoAccount.get("email");
            }
        }
        throw new OAuth2AuthenticationException("이메일을 추출할 수 없습니다.");
    }

    @SuppressWarnings("unchecked")
    private String extractNickname(OAuth2User oauth2User, String registrationId) {
        if ("google".equals(registrationId)) {
            return oauth2User.getAttribute("name");
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> properties = (Map<String, Object>) oauth2User.getAttribute("properties");
            if (properties != null) {
                return (String) properties.get("nickname");
            }
        }
        return "사용자";
    }
}