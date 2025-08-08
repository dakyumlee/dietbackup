package com.mydiet.service;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(String email, String nickname, String provider, String providerId) {
        log.info("새 사용자 생성: {}", email);
        
        try {
            User user = User.builder()
                .email(email)
                .nickname(nickname)
                .provider(provider)
                .providerId(providerId)
                .role(User.Role.USER)
                .emotionMode("다정함")
                .build();

            User saved = userRepository.save(user);
            log.info("사용자 생성 완료: ID={}, 이메일={}", saved.getId(), saved.getEmail());
            
            return saved;
        } catch (Exception e) {
            log.error("사용자 생성 실패: {}", email, e);
            throw new RuntimeException("사용자 생성에 실패했습니다: " + e.getMessage());
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

}