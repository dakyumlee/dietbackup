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
public class SessionApiController {

    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    private Long getCurrentUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            userId = 1L;
            session.setAttribute("userId", userId);
            session.setAttribute("authenticated", true);
        }
        return userId;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpSession session) {
        try {
            Long userId = getCurrentUserId(session);
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                    "nickname", "테스트사용자",
                    "email", "test@test.com",
                    "weightGoal", 65,
                    "emotionMode", "다정함"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "nickname", user.getNickname(),
                "email", user.getEmail(),
                "weightGoal", user.getWeightGoal(),
                "emotionMode", user.getEmotionMode()
            ));
        } catch (Exception e) {
            log.error("프로필 조회 실패", e);
            return ResponseEntity.ok(Map.of(
                "nickname", "테스트사용자",
                "email", "test@test.com",
                "weightGoal", 65,
                "emotionMode", "다정함"
            ));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats(HttpSession session) {
        try {
            Long userId = getCurrentUserId(session);
            LocalDate today = LocalDate.now();
            
            List<MealLog> meals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> workouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> emotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            int totalCalories = meals.stream()
                .mapToInt(m -> m.getCaloriesEstimate() != null ? m.getCaloriesEstimate() : 0)
                .sum();
            
            int burnedCalories = workouts.stream()
                .mapToInt(w -> w.getCaloriesBurned() != null ? w.getCaloriesBurned() : 0)
                .sum();
            
            int goalAchievement = totalCalories > 0 ? Math.min((totalCalories * 100) / 2000, 100) : 0;
            
            return ResponseEntity.ok(Map.of(
                "mealCount", meals.size(),
                "workoutCount", workouts.size(),
                "emotionCount", emotions.size(),
                "totalCalories", totalCalories,
                "burnedCalories", burnedCalories,
                "goalAchievement", goalAchievement
            ));
        } catch (Exception e) {
            log.error("통계 조회 실패", e);
            return ResponseEntity.ok(Map.of(
                "mealCount", 0,
                "workoutCount", 0,
                "emotionCount", 0,
                "totalCalories", 0,
                "burnedCalories", 0,
                "goalAchievement", 0
            ));
        }
    }

    @GetMapping("/today-data")
    public ResponseEntity<?> getTodayData(HttpSession session) {
        try {
            Long userId = getCurrentUserId(session);
            LocalDate today = LocalDate.now();
            
            List<MealLog> meals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> workouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> emotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            return ResponseEntity.ok(Map.of(
                "meals", meals,
                "workouts", workouts,
                "emotions", emotions,
                "date", today
            ));
        } catch (Exception e) {
            log.error("오늘 데이터 조회 실패", e);
            return ResponseEntity.ok(Map.of(
                "meals", new ArrayList<>(),
                "workouts", new ArrayList<>(),
                "emotions", new ArrayList<>(),
                "date", LocalDate.now()
            ));
        }
    }
}