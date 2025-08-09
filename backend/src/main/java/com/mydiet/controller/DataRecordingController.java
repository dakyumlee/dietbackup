package com.mydiet.controller;
import com.mydiet.model.Role;
import com.mydiet.model.User;
import com.mydiet.model.MealLog;
import com.mydiet.model.WorkoutLog;
import com.mydiet.model.EmotionLog;
import com.mydiet.repository.UserRepository;
import com.mydiet.repository.MealLogRepository;
import com.mydiet.repository.WorkoutLogRepository;
import com.mydiet.repository.EmotionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/data-recording")
@RequiredArgsConstructor
public class DataRecordingController {

    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    @PostMapping("/meal")
    public ResponseEntity<Map<String, Object>> saveMeal(@RequestBody Map<String, Object> request) {
        log.info("=== 실제 식단 기록 저장 ===");
        log.info("요청 데이터: {}", request);
        
        try {
            User user = userRepository.findById(1L).orElseThrow(() -> 
                new RuntimeException("사용자를 찾을 수 없습니다"));
            
            MealLog meal = MealLog.builder()
                .user(user)
                .description((String) request.get("description"))
                .caloriesEstimate(request.get("calories") != null ? 
                    Integer.valueOf(request.get("calories").toString()) : null)
                .date(LocalDate.now())
                .build();
            
            MealLog saved = mealLogRepository.save(meal);
            log.info("식단 저장 완료: ID={}, 설명={}", saved.getId(), saved.getDescription());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "식단이 실제 DB에 저장되었습니다!",
                "data", saved
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
    public ResponseEntity<Map<String, Object>> saveWorkout(@RequestBody Map<String, Object> request) {
        log.info("=== 실제 운동 기록 저장 ===");
        log.info("요청 데이터: {}", request);
        
        try {
            User user = userRepository.findById(1L).orElseThrow(() -> 
                new RuntimeException("사용자를 찾을 수 없습니다"));
            
            WorkoutLog workout = WorkoutLog.builder()
                .user(user)
                .type((String) request.get("type"))
                .duration(request.get("duration") != null ? 
                    Integer.valueOf(request.get("duration").toString()) : null)
                .caloriesBurned(request.get("calories") != null ? 
                    Integer.valueOf(request.get("calories").toString()) : null)
                .date(LocalDate.now())
                .build();
            
            WorkoutLog saved = workoutLogRepository.save(workout);
            log.info("운동 저장 완료: ID={}, 운동={}", saved.getId(), saved.getType());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "운동이 실제 DB에 저장되었습니다!",
                "data", saved
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
    public ResponseEntity<Map<String, Object>> saveEmotion(@RequestBody Map<String, Object> request) {
        log.info("=== 실제 감정 기록 저장 ===");
        log.info("요청 데이터: {}", request);
        
        try {
            User user = userRepository.findById(1L).orElseThrow(() -> 
                new RuntimeException("사용자를 찾을 수 없습니다"));
            
            EmotionLog emotion = EmotionLog.builder()
                .user(user)
                .mood((String) request.get("mood"))
                .note((String) request.get("note"))
                .date(LocalDate.now())
                .build();
            
            EmotionLog saved = emotionLogRepository.save(emotion);
            log.info("감정 저장 완료: ID={}, 기분={}", saved.getId(), saved.getMood());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "감정이 실제 DB에 저장되었습니다!",
                "data", saved
            ));
            
        } catch (Exception e) {
            log.error("감정 저장 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayData() {
        log.info("=== 오늘의 데이터 조회 ===");
        
        try {
            User user = userRepository.findById(1L).orElseThrow(() -> 
                new RuntimeException("사용자를 찾을 수 없습니다"));
            
            LocalDate today = LocalDate.now();
            
            var meals = mealLogRepository.findByUserIdAndDate(user.getId(), today);
            var workouts = workoutLogRepository.findByUserIdAndDate(user.getId(), today);
            var emotions = emotionLogRepository.findByUserIdAndDate(user.getId(), today);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "date", today,
                "meals", meals,
                "workouts", workouts,
                "emotions", emotions,
                "counts", Map.of(
                    "meals", meals.size(),
                    "workouts", workouts.size(),
                    "emotions", emotions.size()
                )
            ));
            
        } catch (Exception e) {
            log.error("오늘의 데이터 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}