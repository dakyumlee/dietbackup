package com.mydiet.controller;

import com.mydiet.model.*;
import com.mydiet.repository.*;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/save")
@RequiredArgsConstructor
public class IntegratedSaveController {

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

    @PostMapping("/meal")
    public ResponseEntity<Map<String, Object>> saveMeal(
        @RequestBody Map<String, Object> request,
        HttpSession session) {
        
        log.info("=== 식단 저장 요청 ===");
        log.info("요청 데이터: {}", request);

        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자 인증이 필요합니다"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자를 찾을 수 없습니다"));
            }

            String description = request.get("description") != null ? request.get("description").toString() : "";
            Integer calories = request.get("calories") != null ? 
                Integer.valueOf(request.get("calories").toString()) : 0;

            MealLog meal = new MealLog();
            meal.setUser(user);
            meal.setDescription(description);
            meal.setCaloriesEstimate(calories);
            meal.setDate(LocalDate.now());

            log.info("저장할 식단: {} - {}kcal", description, calories);

            MealLog saved = mealLogRepository.save(meal);
            log.info("식단 저장 성공: ID={}", saved.getId());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "식단이 저장되었습니다",
                "meal", saved
            ));

        } catch (Exception e) {
            log.error("식단 저장 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/workout")
    public ResponseEntity<Map<String, Object>> saveWorkout(
        @RequestBody Map<String, Object> request,
        HttpSession session) {
        
        log.info("=== 운동 저장 요청 ===");
        log.info("요청 데이터: {}", request);

        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자 인증이 필요합니다"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자를 찾을 수 없습니다"));
            }

            String type = request.get("type") != null ? request.get("type").toString() : "";
            Integer duration = request.get("duration") != null ? 
                Integer.valueOf(request.get("duration").toString()) : 0;
            Integer calories = request.get("calories") != null ? 
                Integer.valueOf(request.get("calories").toString()) : 0;

            WorkoutLog workout = new WorkoutLog();
            workout.setUser(user);
            workout.setType(type);
            workout.setDuration(duration);
            workout.setCaloriesBurned(calories);
            workout.setDate(LocalDate.now());

            log.info("저장할 운동: {} - {}분, {}kcal", type, duration, calories);

            WorkoutLog saved = workoutLogRepository.save(workout);
            log.info("운동 저장 성공: ID={}", saved.getId());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "운동이 저장되었습니다",
                "workout", saved
            ));

        } catch (Exception e) {
            log.error("운동 저장 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/emotion")
    public ResponseEntity<Map<String, Object>> saveEmotion(
        @RequestBody Map<String, Object> request,
        HttpSession session) {
        
        log.info("=== 감정 저장 요청 ===");
        log.info("요청 데이터: {}", request);

        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자 인증이 필요합니다"));
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자를 찾을 수 없습니다"));
            }

            String mood = request.get("mood") != null ? request.get("mood").toString() : "";
            String note = request.get("note") != null ? request.get("note").toString() : "";

            EmotionLog emotion = new EmotionLog();
            emotion.setUser(user);
            emotion.setMood(mood);
            emotion.setNote(note);
            emotion.setDate(LocalDate.now());

            log.info("저장할 감정: {} - {}", mood, note);

            EmotionLog saved = emotionLogRepository.save(emotion);
            log.info("감정 저장 성공: ID={}", saved.getId());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "감정이 저장되었습니다",
                "emotion", saved
            ));

        } catch (Exception e) {
            log.error("감정 저장 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}