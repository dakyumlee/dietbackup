package com.mydiet.controller;

import com.mydiet.model.*;
import com.mydiet.repository.*;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/delete")
@RequiredArgsConstructor
public class DeleteController {

    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    private Long getCurrentUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");
        
        if (!Boolean.TRUE.equals(authenticated) || userId == null) {
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) {
                return users.get(0).getId();
            }
            return null;
        }
        return userId;
    }

    @DeleteMapping("/meal/{id}")
    public ResponseEntity<Map<String, Object>> deleteMeal(@PathVariable Long id, HttpSession session) {
        log.info("=== 식단 삭제: ID={} ===", id);

        try {
            Long userId = getCurrentUserId(session);
            
            Optional<MealLog> mealOpt = mealLogRepository.findById(id);
            if (mealOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "식단을 찾을 수 없습니다"
                ));
            }

            MealLog meal = mealOpt.get();
            if (!meal.getUser().getId().equals(userId)) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "권한이 없습니다"
                ));
            }

            mealLogRepository.delete(meal);
            log.info("식단 삭제 완료: {}", meal.getDescription());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "식단이 삭제되었습니다"
            ));

        } catch (Exception e) {
            log.error("식단 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/workout/{id}")
    public ResponseEntity<Map<String, Object>> deleteWorkout(@PathVariable Long id, HttpSession session) {
        log.info("=== 운동 삭제: ID={} ===", id);

        try {
            Long userId = getCurrentUserId(session);
            
            Optional<WorkoutLog> workoutOpt = workoutLogRepository.findById(id);
            if (workoutOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "운동을 찾을 수 없습니다"
                ));
            }

            WorkoutLog workout = workoutOpt.get();
            if (!workout.getUser().getId().equals(userId)) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "권한이 없습니다"
                ));
            }

            workoutLogRepository.delete(workout);
            log.info("운동 삭제 완료: {}", workout.getType());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "운동이 삭제되었습니다"
            ));

        } catch (Exception e) {
            log.error("운동 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/emotion/{id}")
    public ResponseEntity<Map<String, Object>> deleteEmotion(@PathVariable Long id, HttpSession session) {
        log.info("=== 감정 삭제: ID={} ===", id);

        try {
            Long userId = getCurrentUserId(session);
            
            Optional<EmotionLog> emotionOpt = emotionLogRepository.findById(id);
            if (emotionOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "감정을 찾을 수 없습니다"
                ));
            }

            EmotionLog emotion = emotionOpt.get();
            if (!emotion.getUser().getId().equals(userId)) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "권한이 없습니다"
                ));
            }

            emotionLogRepository.delete(emotion);
            log.info("감정 삭제 완료: {}", emotion.getMood());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "감정이 삭제되었습니다"
            ));

        } catch (Exception e) {
            log.error("감정 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/all/today")
    public ResponseEntity<Map<String, Object>> deleteTodayAll(HttpSession session) {
        log.info("=== 오늘 모든 데이터 삭제 ===");

        try {
            Long userId = getCurrentUserId(session);
            LocalDate today = LocalDate.now();
            
            List<MealLog> todayMeals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> todayWorkouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> todayEmotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            int mealCount = todayMeals.size();
            int workoutCount = todayWorkouts.size();
            int emotionCount = todayEmotions.size();
            int totalCount = mealCount + workoutCount + emotionCount;

            if (totalCount == 0) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "삭제할 데이터가 없습니다",
                    "deletedCount", 0
                ));
            }

            mealLogRepository.deleteAll(todayMeals);
            workoutLogRepository.deleteAll(todayWorkouts);
            emotionLogRepository.deleteAll(todayEmotions);
            
            log.info("오늘 모든 데이터 삭제 완료 - 식단: {}개, 운동: {}개, 감정: {}개", 
                    mealCount, workoutCount, emotionCount);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("오늘의 모든 데이터가 삭제되었습니다 (식단: %d개, 운동: %d개, 감정: %d개)", 
                        mealCount, workoutCount, emotionCount),
                "deletedCount", totalCount,
                "mealCount", mealCount,
                "workoutCount", workoutCount,
                "emotionCount", emotionCount
            ));

        } catch (Exception e) {
            log.error("오늘 전체 데이터 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}