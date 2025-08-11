package com.mydiet.controller;

import com.mydiet.model.*;
import com.mydiet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;
    private final UserRepository userRepository;
    
    @GetMapping("/today-data")
    public ResponseEntity<Map<String, Object>> getTodayData(HttpSession session) {
        log.info("=== 오늘 데이터 조회 요청 ===");
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) userId = 1L;
        
        try {
            LocalDate today = LocalDate.now();
            List<MealLog> todayMeals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> todayWorkouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> todayEmotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", today.toString());
            response.put("meals", todayMeals);
            response.put("workouts", todayWorkouts);
            response.put("emotions", todayEmotions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 오늘 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "데이터 조회에 실패했습니다."));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(HttpSession session) {
        log.info("=== 사용자 통계 조회 요청 ===");
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) userId = 1L;

        try {
            LocalDate today = LocalDate.now();
            
            int mealCount = mealLogRepository.findByUserIdAndDate(userId, today).size();
            int workoutCount = workoutLogRepository.findByUserIdAndDate(userId, today).size();
            int emotionCount = emotionLogRepository.findByUserIdAndDate(userId, today).size();
            
            int totalCalories = mealLogRepository.findByUserIdAndDate(userId, today)
                .stream()
                .mapToInt(meal -> meal.getCaloriesEstimate() != null ? meal.getCaloriesEstimate() : 0)
                .sum();
            
            int burnedCalories = workoutLogRepository.findByUserIdAndDate(userId, today)
                .stream()
                .mapToInt(workout -> workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0)
                .sum();
            
            double goalAchievement = totalCalories > 0 ? Math.min((double) totalCalories / 2000 * 100, 100) : 0;
            
            long totalMeals = mealLogRepository.countByUserId(userId);
            long totalWorkouts = workoutLogRepository.countByUserId(userId);
            long totalEmotions = emotionLogRepository.countByUserId(userId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("mealCount", mealCount);
            stats.put("workoutCount", workoutCount);
            stats.put("emotionCount", emotionCount);
            stats.put("totalCalories", totalCalories);
            stats.put("burnedCalories", burnedCalories);
            stats.put("goalAchievement", Math.round(goalAchievement));
            stats.put("totalMeals", totalMeals);
            stats.put("totalWorkouts", totalWorkouts);
            stats.put("totalEmotions", totalEmotions);
            stats.put("accountAge", "신규 사용자");
            stats.put("lastActivity", "오늘");
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("❌ 사용자 통계 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "통계 조회에 실패했습니다."));
        }
    }
}
