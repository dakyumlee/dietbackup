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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("=== OAuth2 사용자 로드 시작 ===");
        
        try {
            OAuth2User oauth2User = super.loadUser(userRequest);
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            String userNameAttributeName = userRequest.getClientRegistration()
                    .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

            log.info("OAuth 제공자: {}", registrationId);
            log.info("사용자 속성 키: {}", userNameAttributeName);
            log.info("OAuth 응답 데이터: {}", oauth2User.getAttributes());

            String email = extractEmail(oauth2User, registrationId);
            String nickname = extractNickname(oauth2User, registrationId);
            String providerId = oauth2User.getName();

            log.info("추출된 정보 - 이메일: {}, 닉네임: {}, 제공자ID: {}", email, nickname, providerId);

            User user = saveOrUpdateUser(email, nickname, providerId, registrationId);
            
            return new OAuth2UserPrincipal(user, oauth2User.getAttributes(), userNameAttributeName);
            
        } catch (Exception e) {
            log.error("OAuth2 사용자 로드 실패", e);
            throw new OAuth2AuthenticationException("OAuth2 사용자 로드 중 오류 발생: " + e.getMessage());
        }
    }

    private User saveOrUpdateUser(String email, String nickname, String providerId, String provider) {
        try {
            Optional<User> existingUser = userRepository.findByEmailOrProviderId(email, providerId);

            if (existingUser.isPresent()) {
                User user = existingUser.get();
                user.setNickname(nickname);
                user.setUpdatedAt(LocalDateTime.now());
                
                log.info("기존 사용자 업데이트: {}", email);
                return userRepository.save(user);
            } else {
                User newUser = User.builder()
                        .email(email)
                        .nickname(nickname)
                        .provider(provider)
                        .providerId(providerId)
                        .role("USER")
                        .emotionMode("다정함")
                        .weightGoal(70.0)
                        .currentWeight(70.0)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                
                log.info("새 사용자 생성: {}", email);
                return userRepository.save(newUser);
            }
        } catch (Exception e) {
            log.error("사용자 저장/업데이트 실패", e);
            throw new RuntimeException("사용자 데이터 처리 중 오류 발생", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractEmail(OAuth2User oauth2User, String registrationId) {
        try {
            if ("google".equals(registrationId)) {
                String email = oauth2User.getAttribute("email");
                if (email == null) {
                    throw new RuntimeException("Google에서 이메일을 가져올 수 없습니다");
                }
                return email;
            } else if ("kakao".equals(registrationId)) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) oauth2User.getAttribute("kakao_account");
                if (kakaoAccount == null) {
                    throw new RuntimeException("Kakao 계정 정보를 가져올 수 없습니다");
                }
                String email = (String) kakaoAccount.get("email");
                if (email == null) {
                    throw new RuntimeException("Kakao에서 이메일을 가져올 수 없습니다");
                }
                return email;
            }
            throw new RuntimeException("지원하지 않는 OAuth 제공자: " + registrationId);
        } catch (Exception e) {
            log.error("이메일 추출 실패: provider={}", registrationId, e);
            throw new RuntimeException("이메일 추출 중 오류 발생", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractNickname(OAuth2User oauth2User, String registrationId) {
        try {
            if ("google".equals(registrationId)) {
                String name = oauth2User.getAttribute("name");
                return name != null ? name : "Google 사용자";
            } else if ("kakao".equals(registrationId)) {
                Map<String, Object> properties = (Map<String, Object>) oauth2User.getAttribute("properties");
                if (properties != null) {
                    String nickname = (String) properties.get("nickname");
                    return nickname != null ? nickname : "Kakao 사용자";
                }
                return "Kakao 사용자";
            }
            return "사용자";
        } catch (Exception e) {
            log.error("닉네임 추출 실패: provider={}", registrationId, e);
            return "사용자";
        }
    }
}