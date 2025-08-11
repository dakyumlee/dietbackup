package com.mydiet.controller;

import com.mydiet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DataController {
    
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;
    
    @DeleteMapping("/meal/{id}")
    public ResponseEntity<Map<String, Object>> deleteMeal(@PathVariable Long id) {
        try {
            if (mealLogRepository.existsById(id)) {
                mealLogRepository.deleteById(id);
                return ResponseEntity.ok(Map.of("success", true, "message", "식단이 삭제되었습니다."));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ 식단 삭제 실패 - ID: {}", id, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "식단 삭제에 실패했습니다."));
        }
    }
    
    @DeleteMapping("/workout/{id}")
    public ResponseEntity<Map<String, Object>> deleteWorkout(@PathVariable Long id) {
        try {
            if (workoutLogRepository.existsById(id)) {
                workoutLogRepository.deleteById(id);
                return ResponseEntity.ok(Map.of("success", true, "message", "운동이 삭제되었습니다."));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ 운동 삭제 실패 - ID: {}", id, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "운동 삭제에 실패했습니다."));
        }
    }
    
    @DeleteMapping("/emotion/{id}")
    public ResponseEntity<Map<String, Object>> deleteEmotion(@PathVariable Long id) {
        try {
            if (emotionLogRepository.existsById(id)) {
                emotionLogRepository.deleteById(id);
                return ResponseEntity.ok(Map.of("success", true, "message", "감정이 삭제되었습니다."));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ 감정 삭제 실패 - ID: {}", id, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "감정 삭제에 실패했습니다."));
        }
    }
}