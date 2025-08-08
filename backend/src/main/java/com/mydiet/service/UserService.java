package com.mydiet.service;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import com.mydiet.dto.request.UpdateProfileRequest;
import com.mydiet.dto.response.UserStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    public User createUser(String email, String nickname, String provider, String providerId) {
        log.info("Creating new user: email={}, provider={}", email, provider);
        
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .provider(provider)
                .providerId(providerId)
                .role(User.Role.USER)
                .build();
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getWeightGoal() != null) {
            user.setWeightGoal(request.getWeightGoal());
        }
        if (request.getCurrentWeight() != null) {
            user.setCurrentWeight(request.getCurrentWeight());
        }
        if (request.getHeight() != null) {
            user.setHeight(request.getHeight());
        }
        if (request.getEmotionMode() != null) {
            user.setEmotionMode(request.getEmotionMode());
        }
        if (request.getDailyCalorieGoal() != null) {
            user.setDailyCalorieGoal(request.getDailyCalorieGoal());
        }
        if (request.getWeeklyWorkoutGoal() != null) {
            user.setWeeklyWorkoutGoal(request.getWeeklyWorkoutGoal());
        }
        if (request.getDailyProteinGoal() != null) {
            user.setDailyProteinGoal(request.getDailyProteinGoal());
        }
        if (request.getDailyWaterGoal() != null) {
            user.setDailyWaterGoal(request.getDailyWaterGoal());
        }
        if (request.getActivityLevel() != null) {
            user.setActivityLevel(request.getActivityLevel());
        }
        
        return userRepository.save(user);
    }
    
    public UserStatsResponse getUserStats(Long userId) {
        log.info("Getting user stats for: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return UserStatsResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .currentWeight(user.getCurrentWeight())
                .weightGoal(user.getWeightGoal())
                .dailyCalorieGoal(user.getDailyCalorieGoal())
                .weeklyWorkoutGoal(user.getWeeklyWorkoutGoal())
                .emotionMode(user.getEmotionMode())
                .bmi(user.getBmi())
                .remainingWeight(user.getRemainingWeight())
                .build();
    }
    
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<User> getRecentUsers(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return userRepository.findRecentUsers(startDate);
    }
    
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }
    
    @Transactional(readOnly = true)
    public long countNewUsers(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return userRepository.countNewUsersFrom(startDate);
    }
    
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);
        userRepository.deleteById(userId);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public User getOrCreateUser(String email, String nickname, String provider, String providerId) {
        return findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> createUser(email, nickname, provider, providerId));
    }
}