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
@RequestMapping("/api/save")
@RequiredArgsConstructor
@Slf4j
public class IntegratedSaveController {

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

    @PostMapping("/meal")
    public ResponseEntity<Map<String, Object>> saveMeal(@RequestBody Map<String, Object> request, HttpSession session) {
        log.info("=== 식단 저장 시작 ===");
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

            MealLog meal = new MealLog();
            meal.setUser(user);
            meal.setDescription(request.get("description").toString());
            
            Integer calories = request.get("caloriesEstimate") != null ? 
                Integer.valueOf(request.get("caloriesEstimate").toString()) : 0;
            meal.setCaloriesEstimate(calories);
            
            String photoUrl = request.get("photoUrl") != null ? 
                request.get("photoUrl").toString() : null;
            meal.setPhotoUrl(photoUrl);
            
            meal.setDate(LocalDate.now());

            log.info("저장할 식단: {} ({}kcal)", meal.getDescription(), meal.getCaloriesEstimate());

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
    public ResponseEntity<Map<String, Object>> saveWorkout(@RequestBody Map<String, Object> request, HttpSession session) {
        log.info("=== 운동 저장 시작 ===");
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

            WorkoutLog workout = new WorkoutLog();
            workout.setUser(user);
            workout.setType(request.get("type").toString());
            
            Integer duration = request.get("duration") != null ? 
                Integer.valueOf(request.get("duration").toString()) : 0;
            workout.setDuration(duration);
            
            Integer caloriesBurned = request.get("caloriesBurned") != null ? 
                Integer.valueOf(request.get("caloriesBurned").toString()) : 0;
            workout.setCaloriesBurned(caloriesBurned);
            
            workout.setDate(LocalDate.now());

            log.info("저장할 운동: {} {}분 ({}kcal)", workout.getType(), workout.getDuration(), workout.getCaloriesBurned());

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
    public ResponseEntity<Map<String, Object>> saveEmotion(@RequestBody Map<String, Object> request, HttpSession session) {
        log.info("=== 감정 저장 시작 ===");
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

            EmotionLog emotion = new EmotionLog();
            emotion.setUser(user);
            emotion.setMood(request.get("mood").toString());
            
            String note = request.get("note") != null ? 
                request.get("note").toString() : "";
            emotion.setNote(note);
            
            emotion.setDate(LocalDate.now());

            log.info("저장할 감정: {} - {}", emotion.getMood(), emotion.getNote());

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