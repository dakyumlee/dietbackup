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
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserDataController {

    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    private Long getCurrentUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                return userId;
            }
        }

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return null;
        }

        User firstUser = users.get(0);
        session.setAttribute("userId", firstUser.getId());
        return firstUser.getId();
    }

    @GetMapping("/today-data")
    public ResponseEntity<Map<String, Object>> getTodayData(HttpSession session) {
        log.info("=== 오늘의 데이터 조회 ===");

        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자 인증이 필요합니다"));
            }

            LocalDate today = LocalDate.now();
            
            List<MealLog> todayMeals = mealLogRepository.findByUserIdAndDate(userId, today);
            
            List<WorkoutLog> todayWorkouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            
            List<EmotionLog> todayEmotions = emotionLogRepository.findByUserIdAndDate(userId, today);

            int totalCaloriesConsumed = todayMeals.stream()
                .mapToInt(meal -> meal.getCaloriesEstimate() != null ? meal.getCaloriesEstimate() : 0)
                .sum();
            
            int totalCaloriesBurned = todayWorkouts.stream()
                .mapToInt(workout -> workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0)
                .sum();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("date", today.toString());
            result.put("meals", todayMeals);
            result.put("workouts", todayWorkouts);
            result.put("emotions", todayEmotions);
            result.put("totalCaloriesConsumed", totalCaloriesConsumed);
            result.put("totalCaloriesBurned", totalCaloriesBurned);
            result.put("netCalories", totalCaloriesConsumed - totalCaloriesBurned);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("오늘의 데이터 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(HttpSession session) {
        log.info("=== 사용자 프로필 조회 ===");

        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자 인증이 필요합니다"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자를 찾을 수 없습니다"));
            }

            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("email", user.getEmail());
            profile.put("nickname", user.getNickname());
            profile.put("weightGoal", user.getWeightGoal());
            profile.put("emotionMode", user.getEmotionMode());
            profile.put("provider", user.getProvider());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "profile", profile
            ));

        } catch (Exception e) {
            log.error("사용자 프로필 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(HttpSession session) {
        log.info("=== 사용자 통계 조회 ===");

        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자 인증이 필요합니다"));
            }

            long totalMeals = mealLogRepository.countByUserId(userId);
            long totalWorkouts = workoutLogRepository.countByUserId(userId);
            long totalEmotions = emotionLogRepository.countByUserId(userId);

            LocalDate weekStart = LocalDate.now().minusDays(7);
            long weeklyMeals = mealLogRepository.countByUserIdAndDateAfter(userId, weekStart);
            long weeklyWorkouts = workoutLogRepository.countByUserIdAndDateAfter(userId, weekStart);
            long weeklyEmotions = emotionLogRepository.countByUserIdAndDateAfter(userId, weekStart);

            LocalDate today = LocalDate.now();
            long todayMeals = mealLogRepository.countByUserIdAndDate(userId, today);
            long todayWorkouts = workoutLogRepository.countByUserIdAndDate(userId, today);
            long todayEmotions = emotionLogRepository.countByUserIdAndDate(userId, today);

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", Map.of(
                "meals", totalMeals,
                "workouts", totalWorkouts,
                "emotions", totalEmotions
            ));
            stats.put("weekly", Map.of(
                "meals", weeklyMeals,
                "workouts", weeklyWorkouts,
                "emotions", weeklyEmotions
            ));
            stats.put("today", Map.of(
                "meals", todayMeals,
                "workouts", todayWorkouts,
                "emotions", todayEmotions
            ));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats
            ));

        } catch (Exception e) {
            log.error("사용자 통계 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> request, HttpSession session) {
        log.info("=== 프로필 업데이트 ===");

        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자 인증이 필요합니다"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자를 찾을 수 없습니다"));
            }

            if (request.containsKey("nickname")) {
                user.setNickname(request.get("nickname").toString());
            }
            if (request.containsKey("weightGoal")) {
                user.setWeightGoal(Double.valueOf(request.get("weightGoal").toString()));
            }
            if (request.containsKey("emotionMode")) {
                user.setEmotionMode(request.get("emotionMode").toString());
            }

            User savedUser = userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "프로필이 업데이트되었습니다",
                "user", savedUser
            ));

        } catch (Exception e) {
            log.error("프로필 업데이트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}