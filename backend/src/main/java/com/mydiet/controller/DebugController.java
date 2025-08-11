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

@RestController
@RequestMapping("/api/debug-fixed")
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    @GetMapping("/user-data/{userId}")
    public ResponseEntity<Map<String, Object>> getUserData(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            List<MealLog> allMeals = mealLogRepository.findByUserId(user.getId());
            List<WorkoutLog> allWorkouts = workoutLogRepository.findByUserId(user.getId());
            List<EmotionLog> allEmotions = emotionLogRepository.findByUserId(user.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("meals", allMeals);
            result.put("workouts", allWorkouts);
            result.put("emotions", allEmotions);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("사용자 데이터 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.minusDays(7);

            long totalUsers = userRepository.count();
            long totalMeals = mealLogRepository.count();
            long totalWorkouts = workoutLogRepository.count();
            long totalEmotions = emotionLogRepository.count();

            List<MealLog> todayMealsList = mealLogRepository.findAll().stream()
                .filter(meal -> meal.getDate().equals(today))
                .toList();
            List<WorkoutLog> todayWorkoutsList = workoutLogRepository.findAll().stream()
                .filter(workout -> workout.getDate().equals(today))
                .toList();
            List<EmotionLog> todayEmotionsList = emotionLogRepository.findAll().stream()
                .filter(emotion -> emotion.getDate().equals(today))
                .toList();

            long todayMeals = todayMealsList.size();
            long todayWorkouts = todayWorkoutsList.size();
            long todayEmotions = todayEmotionsList.size();

            List<MealLog> weekMealsList = mealLogRepository.findAll().stream()
                .filter(meal -> !meal.getDate().isBefore(weekStart) && !meal.getDate().isAfter(today))
                .toList();
            List<WorkoutLog> weekWorkoutsList = workoutLogRepository.findAll().stream()
                .filter(workout -> !workout.getDate().isBefore(weekStart) && !workout.getDate().isAfter(today))
                .toList();
            List<EmotionLog> weekEmotionsList = emotionLogRepository.findAll().stream()
                .filter(emotion -> !emotion.getDate().isBefore(weekStart) && !emotion.getDate().isAfter(today))
                .toList();

            long weekMeals = weekMealsList.size();
            long weekWorkouts = weekWorkoutsList.size();
            long weekEmotions = weekEmotionsList.size();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("totalMeals", totalMeals);
            stats.put("totalWorkouts", totalWorkouts);
            stats.put("totalEmotions", totalEmotions);
            stats.put("todayMeals", todayMeals);
            stats.put("todayWorkouts", todayWorkouts);
            stats.put("todayEmotions", todayEmotions);
            stats.put("weekMeals", weekMeals);
            stats.put("weekWorkouts", weekWorkouts);
            stats.put("weekEmotions", weekEmotions);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("통계 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}