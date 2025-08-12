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
        log.info("=== 세션 정보 디버그 ===");
        
        if (session == null) {
            log.warn("세션이 null입니다");
            return null;
        }
        
        log.info("세션 ID: {}", session.getId());
        log.info("세션 속성들:");
        
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            Object value = session.getAttribute(name);
            log.info("  {} = {}", name, value);
        }
        
        Long userId = (Long) session.getAttribute("userId");
        log.info("추출된 userId: {}", userId);
        
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                log.info("사용자 찾음: {}", userOpt.get().getNickname());
                return userId;
            } else {
                log.warn("userId {}에 해당하는 사용자를 찾을 수 없음", userId);
            }
        }

        log.warn("세션에 userId가 없음. 대체 로직 실행");
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.error("시스템에 사용자가 없음");
            return null;
        }

        User firstUser = users.get(0);
        log.info("첫 번째 사용자 사용: {}", firstUser.getNickname());
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

    @PostMapping("/all-test")
    public ResponseEntity<Map<String, Object>> saveAllTest(HttpSession session) {
        log.info("=== 전체 저장 테스트 ===");

        Map<String, Object> results = new HashMap<>();

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
            meal.setDescription("전체 테스트 식단");
            meal.setCaloriesEstimate(400);
            meal.setDate(LocalDate.now());
            MealLog savedMeal = mealLogRepository.save(meal);
            results.put("meal", savedMeal);

            WorkoutLog workout = new WorkoutLog();
            workout.setUser(user);
            workout.setType("전체 테스트 운동");
            workout.setDuration(25);
            workout.setCaloriesBurned(120);
            workout.setDate(LocalDate.now());
            WorkoutLog savedWorkout = workoutLogRepository.save(workout);
            results.put("workout", savedWorkout);

            EmotionLog emotion = new EmotionLog();
            emotion.setUser(user);
            emotion.setMood("기쁨");
            emotion.setNote("전체 테스트 감정");
            emotion.setDate(LocalDate.now());
            EmotionLog savedEmotion = emotionLogRepository.save(emotion);
            results.put("emotion", savedEmotion);

            results.put("success", true);
            results.put("message", "모든 데이터가 성공적으로 저장되었습니다!");

            log.info("전체 저장 테스트 완료");
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("전체 저장 테스트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}