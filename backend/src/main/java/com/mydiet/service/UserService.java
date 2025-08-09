package com.mydiet.service;

import com.mydiet.model.Role;
import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User createDefaultUser() {
        log.info("기본 사용자 생성 시작");
        
        User user = User.builder()
            .nickname("기본 사용자")
            .email("default@mydiet.com")
            .role(Role.USER)
            .emotionMode("다정함")
            .weightGoal(70.0)
            .provider("LOCAL")
            .createdAt(LocalDateTime.now())
            .build();
        
        User saved = userRepository.save(user);
        log.info("기본 사용자 생성 완료: {}", saved.getEmail());
        
        return saved;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public long count() {
        return userRepository.count();
    }
}