package com.mydiet.service;

import com.mydiet.model.*;
import com.mydiet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    
    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        
        long totalUsers = userRepository.count();
        long totalMeals = mealLogRepository.count();
        long totalWorkouts = workoutLogRepository.count();
        long totalEmotions = emotionLogRepository.count();
        
        List<MealLog> todayMeals = mealLogRepository.findByDate(today);
        List<WorkoutLog> todayWorkouts = workoutLogRepository.findByDate(today);
        List<EmotionLog> todayEmotions = emotionLogRepository.findByDate(today);
        
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", totalUsers);
        stats.put("totalMeals", totalMeals);
        stats.put("totalWorkouts", totalWorkouts);
        stats.put("totalEmotions", totalEmotions);
        stats.put("todayMeals", todayMeals.size());
        stats.put("todayWorkouts", todayWorkouts.size());
        stats.put("todayEmotions", todayEmotions.size());
        
        return stats;
    }

    public List<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        return users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("nickname", user.getNickname());
            userMap.put("email", user.getEmail());
            userMap.put("role", user.getRole());
            userMap.put("createdAt", user.getCreatedAt());
            userMap.put("weightGoal", user.getWeightGoal());
            userMap.put("emotionMode", user.getEmotionMode());
            return userMap;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getUserDetail(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + userId);
        }
        
        User user = userOpt.get();
        
        List<MealLog> meals = mealLogRepository.findByUserIdOrderByDateDesc(userId);
        List<WorkoutLog> workouts = workoutLogRepository.findByUserIdOrderByDateDesc(userId);
        List<EmotionLog> emotions = emotionLogRepository.findByUserIdOrderByDateDesc(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("meals", meals);
        result.put("workouts", workouts);
        result.put("emotions", emotions);
        result.put("mealCount", meals.size());
        result.put("workoutCount", workouts.size());
        result.put("emotionCount", emotions.size());
        
        return result;
    }
}