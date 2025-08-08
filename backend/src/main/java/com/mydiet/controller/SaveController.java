package com.mydiet.controller;

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
@RequestMapping("/api/save")
@RequiredArgsConstructor
public class SaveController {

    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    @PostMapping("/meal")
    public ResponseEntity<Map<String, Object>> saveMeal(@RequestBody Map<String, Object> request) {
        log.info("=== 식단 저장 요청 ===");
        
        try {
            User user = userRepository.findById(1L).orElseThrow(() -> 
                new RuntimeException("사용자를 찾을 수 없습니다"));
            
            String description = (String) request.get("description");
            Integer calories = null;
            
            if (request.get("caloriesEstimate") != null) {
                calories = Integer.valueOf(request.get("caloriesEstimate").toString());
            } else if (request.get("calories") != null) {
                calories = Integer.valueOf(request.get("calories").toString());
            }
            
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "식단 설명을 입력해주세요"
                ));
            }
            
            MealLog meal = MealLog.builder()
                .user(user)
                .description(description.trim())
                .caloriesEstimate(calories)
                .date(LocalDate.now())
                .build();
            
            MealLog saved = mealLogRepository.save(meal);
            log.info("식단 저장 완료: ID={}, 설명={}", saved.getId(), saved.getDescription());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "식단이 저장되었습니다",
                "data", saved
            ));
            
        } catch (Exception e) {
            log.error("식단 저장 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "식단 저장에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/workout")
    public ResponseEntity<Map<String, Object>> saveWorkout(@RequestBody Map<String, Object> request) {
        log.info("=== 운동 저장 요청 ===");
        
        try {
            User user = userRepository.findById(1L).orElseThrow(() -> 
                new RuntimeException("사용자를 찾을 수 없습니다"));
            
            String type = (String) request.get("type");
            Integer duration = request.get("duration") != null ? 
                Integer.valueOf(request.get("duration").toString()) : null;
            Integer caloriesBurned = request.get("caloriesBurned") != null ? 
                Integer.valueOf(request.get("caloriesBurned").toString()) : null;
            
            if (type == null || type.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "운동 종류를 선택해주세요"
                ));
            }
            
            WorkoutLog workout = WorkoutLog.builder()
                .user(user)
                .type(type.trim())
                .duration(duration)
                .caloriesBurned(caloriesBurned)
                .date(LocalDate.now())
                .build();
            
            WorkoutLog saved = workoutLogRepository.save(workout);
            log.info("운동 저장 완료: ID={}, 종류={}", saved.getId(), saved.getType());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "운동이 저장되었습니다",
                "data", saved
            ));
            
        } catch (Exception e) {
            log.error("운동 저장 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "운동 저장에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/emotion")
    public ResponseEntity<Map<String, Object>> saveEmotion(@RequestBody Map<String, Object> request) {
        log.info("=== 감정 저장 요청 ===");
        
        try {
            User user = userRepository.findById(1L).orElseThrow(() -> 
                new RuntimeException("사용자를 찾을 수 없습니다"));
            
            String mood = (String) request.get("mood");
            String note = (String) request.get("note");
            
            if (mood == null || mood.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "감정 상태를 선택해주세요"
                ));
            }
            
            EmotionLog emotion = EmotionLog.builder()
                .user(user)
                .mood(mood.trim())
                .note(note != null ? note.trim() : "")
                .date(LocalDate.now())
                .build();
            
            EmotionLog saved = emotionLogRepository.save(emotion);
            log.info("감정 저장 완료: ID={}, 기분={}", saved.getId(), saved.getMood());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "감정이 저장되었습니다",
                "data", saved
            ));
            
        } catch (Exception e) {
            log.error("감정 저장 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "감정 저장에 실패했습니다: " + e.getMessage()
            ));
        }
    }
}