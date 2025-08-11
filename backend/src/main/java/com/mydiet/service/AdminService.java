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
        log.info("=== 관리자 대시보드 통계 조회 시작 ===");
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
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
            
            log.info("통계 조회 완료 - 사용자: {}, 식단: {}, 운동: {}, 감정: {}", 
                    totalUsers, totalMeals, totalWorkouts, totalEmotions);
            
            return stats;
        } catch (Exception e) {
            log.error("관리자 통계 조회 실패", e);
            throw new RuntimeException("통계 조회에 실패했습니다: " + e.getMessage());
        }
    }

    public Map<String, Object> getAllData() {
        log.info("=== 전체 데이터 조회 시작 ===");
        
        try {
            List<User> users = userRepository.findAll();
            List<MealLog> meals = mealLogRepository.findAll();
            List<WorkoutLog> workouts = workoutLogRepository.findAll();
            List<EmotionLog> emotions = emotionLogRepository.findAll();
            
            log.info("데이터 조회 완료 - 사용자: {}, 식단: {}, 운동: {}, 감정: {}", 
                    users.size(), meals.size(), workouts.size(), emotions.size());
            
            Map<String, Object> data = new HashMap<>();
            data.put("users", users);
            data.put("meals", meals);
            data.put("workouts", workouts);
            data.put("emotions", emotions);
            data.put("timestamp", new Date());
            
            return data;
        } catch (Exception e) {
            log.error("전체 데이터 조회 실패", e);
            throw new RuntimeException("데이터 조회에 실패했습니다: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getAllUsers() {
        log.info("=== 전체 사용자 목록 조회 ===");
        
        try {
            List<User> users = userRepository.findAll();
            
            return users.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("nickname", user.getNickname());
                userMap.put("email", user.getEmail());
                userMap.put("role", user.getRole());
                userMap.put("createdAt", user.getCreatedAt());
                userMap.put("weightGoal", user.getWeightGoal());
                userMap.put("currentWeight", user.getCurrentWeight());
                userMap.put("emotionMode", user.getEmotionMode());
                userMap.put("provider", user.getProvider());
                return userMap;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            throw new RuntimeException("사용자 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }

    public Map<String, Object> getUserDetail(Long userId) {
        log.info("=== 사용자 상세 조회: userId={} ===", userId);
        
        try {
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
            
            log.info("사용자 상세 조회 완료: {}", user.getEmail());
            
            return result;
        } catch (Exception e) {
            log.error("사용자 상세 조회 실패: userId={}", userId, e);
            throw new RuntimeException("사용자 상세 조회에 실패했습니다: " + e.getMessage());
        }
    }
}