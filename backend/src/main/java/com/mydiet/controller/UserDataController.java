package com.mydiet.controller;

import com.mydiet.model.*;
import com.mydiet.repository.*;
import com.mydiet.util.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class UserDataController {

    private final SessionUtil sessionUtil;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;
    private final UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(HttpServletRequest request) {
        try {
            Long userId = sessionUtil.getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
            }

            LocalDate today = LocalDate.now();
            
            List<MealLog> todayMeals = mealLogRepository.findByUserIdAndDate(userId, today);
            int totalCalories = todayMeals.stream()
                .mapToInt(meal -> meal.getCaloriesEstimate() != null ? meal.getCaloriesEstimate() : 0)
                .sum();
            
            List<WorkoutLog> todayWorkouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            int totalBurnedCalories = todayWorkouts.stream()
                .mapToInt(workout -> workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0)
                .sum();
            
            List<EmotionLog> todayEmotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            String recentMood = todayEmotions.isEmpty() ? null : 
                todayEmotions.get(todayEmotions.size() - 1).getMood();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCalories", totalCalories);
            stats.put("totalBurnedCalories", totalBurnedCalories);
            stats.put("mealCount", todayMeals.size());
            stats.put("workoutCount", todayWorkouts.size());
            stats.put("emotionCount", todayEmotions.size());
            stats.put("recentMood", recentMood);
            stats.put("date", today.toString());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("대시보드 통계 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "통계 조회에 실패했습니다."));
        }
    }

    @GetMapping("/today-data")
    public ResponseEntity<Map<String, Object>> getTodayData(HttpServletRequest request) {
        try {
            Long userId = sessionUtil.getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
            }

            LocalDate today = LocalDate.now();
            
            List<MealLog> meals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> workouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> emotions = emotionLogRepository.findByUserIdAndDate(userId, today);

            Map<String, Object> data = new HashMap<>();
            data.put("meals", meals);
            data.put("workouts", workouts);
            data.put("emotions", emotions);
            data.put("date", today.toString());

            return ResponseEntity.ok(data);

        } catch (Exception e) {
            log.error("오늘 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "데이터 조회에 실패했습니다."));
        }
    }

    @GetMapping("/week-data")
    public ResponseEntity<Map<String, Object>> getWeekData(HttpServletRequest request) {
        try {
            Long userId = sessionUtil.getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
            }

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);

            List<MealLog> meals = mealLogRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
            List<WorkoutLog> workouts = workoutLogRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
            List<EmotionLog> emotions = emotionLogRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

            Map<String, Object> data = new HashMap<>();
            data.put("meals", meals);
            data.put("workouts", workouts);
            data.put("emotions", emotions);
            data.put("startDate", startDate.toString());
            data.put("endDate", endDate.toString());

            return ResponseEntity.ok(data);

        } catch (Exception e) {
            log.error("주간 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "주간 데이터 조회에 실패했습니다."));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, Object> profileData,
            HttpServletRequest request) {
        try {
            Long userId = sessionUtil.getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
            }

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            if (profileData.containsKey("nickname")) {
                user.setNickname((String) profileData.get("nickname"));
            }
            if (profileData.containsKey("weightGoal")) {
                Object weightGoal = profileData.get("weightGoal");
                if (weightGoal instanceof Number) {
                    user.setWeightGoal(((Number) weightGoal).doubleValue());
                }
            }
            if (profileData.containsKey("emotionMode")) {
                user.setEmotionMode((String) profileData.get("emotionMode"));
            }

            User savedUser = userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필이 업데이트되었습니다.");
            response.put("user", Map.of(
                "id", savedUser.getId(),
                "nickname", savedUser.getNickname(),
                "email", savedUser.getEmail(),
                "weightGoal", savedUser.getWeightGoal(),
                "emotionMode", savedUser.getEmotionMode()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("프로필 업데이트 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "프로필 업데이트에 실패했습니다."));
        }
    }

    @GetMapping("/activity-summary")
    public ResponseEntity<Map<String, Object>> getActivitySummary(HttpServletRequest request) {
        try {
            Long userId = sessionUtil.getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
            }

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);

            Map<String, Map<String, Integer>> dailyStats = new HashMap<>();
            
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                List<MealLog> dailyMeals = mealLogRepository.findByUserIdAndDate(userId, date);
                List<WorkoutLog> dailyWorkouts = workoutLogRepository.findByUserIdAndDate(userId, date);
                
                int dailyCalories = dailyMeals.stream()
                    .mapToInt(meal -> meal.getCaloriesEstimate() != null ? meal.getCaloriesEstimate() : 0)
                    .sum();
                
                int dailyBurnedCalories = dailyWorkouts.stream()
                    .mapToInt(workout -> workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0)
                    .sum();

                Map<String, Integer> dayData = new HashMap<>();
                dayData.put("calories", dailyCalories);
                dayData.put("burnedCalories", dailyBurnedCalories);
                dayData.put("mealCount", dailyMeals.size());
                dayData.put("workoutCount", dailyWorkouts.size());
                
                dailyStats.put(date.toString(), dayData);
            }

            Map<String, Object> summary = new HashMap<>();
            summary.put("dailyStats", dailyStats);
            summary.put("startDate", startDate.toString());
            summary.put("endDate", endDate.toString());

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            log.error("활동 요약 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "활동 요약 조회에 실패했습니다."));
        }
    }
}