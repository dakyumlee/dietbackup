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
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DeleteController {

    private final SessionUtil sessionUtil;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    @DeleteMapping("/meal/{mealId}")
    public ResponseEntity<Map<String, Object>> deleteMeal(@PathVariable Long mealId, HttpServletRequest request) {
        log.info("식단 삭제 요청: mealId={}", mealId);

        try {
            Long userId = sessionUtil.getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
                ));
            }
            
            Optional<MealLog> mealOpt = mealLogRepository.findById(mealId);
            if (mealOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "식단을 찾을 수 없습니다."
                ));
            }

            MealLog meal = mealOpt.get();
            
            if (!meal.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "삭제 권한이 없습니다."
                ));
            }

            String description = meal.getDescription();
            mealLogRepository.delete(meal);
            
            log.info("식단 삭제 완료: {}", description);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "식단이 삭제되었습니다: " + description
            ));

        } catch (Exception e) {
            log.error("식단 삭제 실패: mealId={}", mealId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/workout/{workoutId}")
    public ResponseEntity<Map<String, Object>> deleteWorkout(@PathVariable Long workoutId, HttpServletRequest request) {
        log.info("운동 삭제 요청: workoutId={}", workoutId);

        try {
            Long userId = sessionUtil.getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
                ));
            }
            
            Optional<WorkoutLog> workoutOpt = workoutLogRepository.findById(workoutId);
            if (workoutOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "운동을 찾을 수 없습니다."
                ));
            }

            WorkoutLog workout = workoutOpt.get();
            
            if (!workout.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "삭제 권한이 없습니다."
                ));
            }

            String type = workout.getType();
            workoutLogRepository.delete(workout);
            
            log.info("운동 삭제 완료: {}", type);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "운동이 삭제되었습니다: " + type
            ));

        } catch (Exception e) {
            log.error("운동 삭제 실패: workoutId={}", workoutId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/emotion/{emotionId}")
    public ResponseEntity<Map<String, Object>> deleteEmotion(@PathVariable Long emotionId, HttpServletRequest request) {
        log.info("감정 삭제 요청: emotionId={}", emotionId);

        try {
            Long userId = sessionUtil.getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
                ));
            }
            
            Optional<EmotionLog> emotionOpt = emotionLogRepository.findById(emotionId);
            if (emotionOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "감정 기록을 찾을 수 없습니다."
                ));
            }

            EmotionLog emotion = emotionOpt.get();
            
            if (!emotion.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "삭제 권한이 없습니다."
                ));
            }

            String mood = emotion.getMood();
            emotionLogRepository.delete(emotion);
            
            log.info("감정 삭제 완료: {}", mood);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "감정 기록이 삭제되었습니다: " + mood
            ));

        } catch (Exception e) {
            log.error("감정 삭제 실패: emotionId={}", emotionId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/today-all")
    public ResponseEntity<Map<String, Object>> deleteTodayAll(HttpServletRequest request) {
        log.info("오늘 데이터 전체 삭제 요청");

        try {
            Long userId = sessionUtil.getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
                ));
            }

            LocalDate today = LocalDate.now();
            
            var todayMeals = mealLogRepository.findByUserIdAndDate(userId, today);
            var todayWorkouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            var todayEmotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            int deletedMeals = todayMeals.size();
            int deletedWorkouts = todayWorkouts.size();
            int deletedEmotions = todayEmotions.size();
            
            mealLogRepository.deleteAll(todayMeals);
            workoutLogRepository.deleteAll(todayWorkouts);
            emotionLogRepository.deleteAll(todayEmotions);
            
            log.info("오늘 데이터 전체 삭제 완료: 식단={}, 운동={}, 감정={}", 
                deletedMeals, deletedWorkouts, deletedEmotions);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("오늘의 모든 데이터가 삭제되었습니다. (식단: %d개, 운동: %d개, 감정: %d개)", 
                    deletedMeals, deletedWorkouts, deletedEmotions),
                "deletedCounts", Map.of(
                    "meals", deletedMeals,
                    "workouts", deletedWorkouts,
                    "emotions", deletedEmotions
                )
            ));

        } catch (Exception e) {
            log.error("오늘 데이터 전체 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}