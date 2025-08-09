package com.mydiet.controller;
import com.mydiet.model.Role;
import com.mydiet.repository.MealLogRepository;
import com.mydiet.repository.EmotionLogRepository;
import com.mydiet.repository.WorkoutLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final MealLogRepository mealLogRepository;
    private final EmotionLogRepository emotionLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("=== 실제 대시보드 통계 조회 ===");
        
        try {
            Long userId = 1L;
            LocalDate today = LocalDate.now();
            
            var meals = mealLogRepository.findByUserIdAndDate(userId, today);
            var workouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            var emotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            int totalCalories = meals.stream()
                .mapToInt(meal -> meal.getCaloriesEstimate() != null ? meal.getCaloriesEstimate() : 0)
                .sum();
            
            int burnedCalories = workouts.stream()
                .mapToInt(workout -> workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0)
                .sum();
            
            double goalAchievement = totalCalories > 0 ? Math.min((double) totalCalories / 2000 * 100, 100) : 0;
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("mealCount", meals.size());
            stats.put("workoutCount", workouts.size());
            stats.put("emotionCount", emotions.size());
            stats.put("totalCalories", totalCalories);
            stats.put("burnedCalories", burnedCalories);
            stats.put("goalAchievement", Math.round(goalAchievement));
            stats.put("netCalories", totalCalories - burnedCalories);
            stats.put("message", "실제 데이터 기반 통계");
            stats.put("date", today);
            
            stats.put("recentMeals", meals.stream()
                .map(meal -> Map.of(
                    "description", meal.getDescription(),
                    "calories", meal.getCaloriesEstimate() != null ? meal.getCaloriesEstimate() : 0
                ))
                .toList());
            
            stats.put("recentWorkouts", workouts.stream()
                .map(workout -> Map.of(
                    "type", workout.getType(),
                    "duration", workout.getDuration() != null ? workout.getDuration() : 0,
                    "calories", workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0
                ))
                .toList());
            
            stats.put("recentEmotions", emotions.stream()
                .map(emotion -> Map.of(
                    "mood", emotion.getMood(),
                    "note", emotion.getNote() != null ? emotion.getNote() : ""
                ))
                .toList());
            
            log.info("실제 통계: 식단 {}개, 운동 {}개, 감정 {}개, 총 칼로리 {}, 소모 칼로리 {}", 
                    meals.size(), workouts.size(), emotions.size(), totalCalories, burnedCalories);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("대시보드 통계 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage(),
                "message", "통계 조회에 실패했습니다"
            ));
        }
    }
}