package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import com.mydiet.repository.MealLogRepository;
import com.mydiet.repository.WorkoutLogRepository;
import com.mydiet.repository.EmotionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserStatsController {

    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        log.info("=== 사용자 프로필 조회 ===");
        
        try {
            User user = userRepository.findById(1L).orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "nickname", "사용자",
                    "email", "",
                    "weightGoal", 65.0,
                    "emotionMode", "다정함"
                ));
            }
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("nickname", user.getNickname());
            profile.put("email", user.getEmail());
            profile.put("weightGoal", user.getWeightGoal());
            profile.put("emotionMode", user.getEmotionMode());
            profile.put("role", user.getRole());
            profile.put("createdAt", user.getCreatedAt());
            profile.put("updatedAt", user.getUpdatedAt());
            
            return ResponseEntity.ok(profile);
            
        } catch (Exception e) {
            log.error("프로필 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "프로필 조회에 실패했습니다"
            ));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(@RequestBody Map<String, Object> request) {
        log.info("=== 사용자 프로필 업데이트 ===");
        
        try {
            User user = userRepository.findById(1L).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "사용자를 찾을 수 없습니다"
                ));
            }
            
            if (request.get("nickname") != null) {
                user.setNickname((String) request.get("nickname"));
            }
            if (request.get("email") != null) {
                user.setEmail((String) request.get("email"));
            }
            if (request.get("weightGoal") != null) {
                user.setWeightGoal(Double.valueOf(request.get("weightGoal").toString()));
            }
            if (request.get("emotionMode") != null) {
                user.setEmotionMode((String) request.get("emotionMode"));
            }
            
            User saved = userRepository.save(user);
            log.info("프로필 업데이트 완료: {}", saved.getNickname());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "프로필이 성공적으로 업데이트되었습니다",
                "user", saved
            ));
            
        } catch (Exception e) {
            log.error("프로필 업데이트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "프로필 업데이트에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        log.info("=== 사용자 통계 조회 ===");
        
        try {
            Long userId = 1L;
            LocalDate today = LocalDate.now();
            
            var meals = mealLogRepository.findByUserIdAndDate(userId, today);
            var workouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            var emotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            int consumedCalories = meals.stream()
                .mapToInt(meal -> meal.getCaloriesEstimate() != null ? meal.getCaloriesEstimate() : 0)
                .sum();
            
            int burnedCalories = workouts.stream()
                .mapToInt(workout -> workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0)
                .sum();
            
            int workoutMinutes = workouts.stream()
                .mapToInt(workout -> workout.getDuration() != null ? workout.getDuration() : 0)
                .sum();
            
            int goalCalories = 2000;
            int goalAchievement = Math.min((consumedCalories * 100) / goalCalories, 100);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("mealCount", meals.size());
            stats.put("workoutCount", workouts.size());
            stats.put("emotionCount", emotions.size());
            stats.put("consumedCalories", consumedCalories);
            stats.put("burnedCalories", burnedCalories);
            stats.put("workoutMinutes", workoutMinutes);
            stats.put("goalAchievement", goalAchievement);
            stats.put("netCalories", consumedCalories - burnedCalories);
            stats.put("date", today);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("통계 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "통계 조회에 실패했습니다"
            ));
        }
    }

    @GetMapping("/today-data")
    public ResponseEntity<Map<String, Object>> getTodayData() {
        log.info("=== 오늘의 데이터 조회 시작 ===");
        
        try {
            Long userId = 1L;
            LocalDate today = LocalDate.now();
            log.info("조회 조건: userId={}, date={}", userId, today);
            
            java.util.List<Object> meals = new java.util.ArrayList<>();
            java.util.List<Object> workouts = new java.util.ArrayList<>();
            java.util.List<Object> emotions = new java.util.ArrayList<>();
            
            try {
                var allMeals = mealLogRepository.findAll();
                log.info("전체 식단 개수: {}", allMeals.size());
                
                for (var meal : allMeals) {
                    try {
                        if (meal.getUser() != null && 
                            meal.getUser().getId() != null &&
                            meal.getUser().getId().equals(userId) && 
                            meal.getDate() != null && 
                            meal.getDate().equals(today)) {
                            
                            Map<String, Object> mealData = new HashMap<>();
                            mealData.put("id", meal.getId());
                            mealData.put("description", meal.getDescription());
                            mealData.put("caloriesEstimate", meal.getCaloriesEstimate());
                            mealData.put("date", meal.getDate().toString());
                            meals.add(mealData);
                        }
                    } catch (Exception e) {
                        log.warn("식단 데이터 처리 중 에러: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("식단 조회 실패", e);
            }
            
            try {
                var allWorkouts = workoutLogRepository.findAll();
                log.info("전체 운동 개수: {}", allWorkouts.size());
                
                for (var workout : allWorkouts) {
                    try {
                        if (workout.getUser() != null && 
                            workout.getUser().getId() != null &&
                            workout.getUser().getId().equals(userId) && 
                            workout.getDate() != null && 
                            workout.getDate().equals(today)) {
                            
                            Map<String, Object> workoutData = new HashMap<>();
                            workoutData.put("id", workout.getId());
                            workoutData.put("type", workout.getType());
                            workoutData.put("duration", workout.getDuration());
                            workoutData.put("caloriesBurned", workout.getCaloriesBurned());
                            workoutData.put("date", workout.getDate().toString());
                            workouts.add(workoutData);
                        }
                    } catch (Exception e) {
                        log.warn("운동 데이터 처리 중 에러: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("운동 조회 실패", e);
            }
            
            try {
                var allEmotions = emotionLogRepository.findAll();
                log.info("전체 감정 개수: {}", allEmotions.size());
                
                for (var emotion : allEmotions) {
                    try {
                        if (emotion.getUser() != null && 
                            emotion.getUser().getId() != null &&
                            emotion.getUser().getId().equals(userId) && 
                            emotion.getDate() != null && 
                            emotion.getDate().equals(today)) {
                            
                            Map<String, Object> emotionData = new HashMap<>();
                            emotionData.put("id", emotion.getId());
                            emotionData.put("mood", emotion.getMood());
                            emotionData.put("note", emotion.getNote());
                            emotionData.put("date", emotion.getDate().toString());
                            emotions.add(emotionData);
                        }
                    } catch (Exception e) {
                        log.warn("감정 데이터 처리 중 에러: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("감정 조회 실패", e);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("meals", meals);
            data.put("workouts", workouts);
            data.put("emotions", emotions);
            data.put("date", today.toString());
            data.put("counts", Map.of(
                "meals", meals.size(),
                "workouts", workouts.size(),
                "emotions", emotions.size()
            ));
            
            log.info("=== 조회 완료: 식단 {}개, 운동 {}개, 감정 {}개 ===", 
                    meals.size(), workouts.size(), emotions.size());
            
            return ResponseEntity.ok(data);
            
        } catch (Exception e) {
            log.error("전체 조회 실패", e);
            
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("meals", java.util.Collections.emptyList());
            emptyData.put("workouts", java.util.Collections.emptyList());
            emptyData.put("emotions", java.util.Collections.emptyList());
            emptyData.put("date", LocalDate.now().toString());
            emptyData.put("counts", Map.of("meals", 0, "workouts", 0, "emotions", 0));
            emptyData.put("error", "데이터 조회 중 오류: " + e.getMessage());
            
            return ResponseEntity.ok(emptyData);
        }
    }
}