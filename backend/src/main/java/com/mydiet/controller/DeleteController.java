package com.mydiet.controller;

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
@RequestMapping("/api/delete")
@RequiredArgsConstructor
public class DeleteController {

    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    @DeleteMapping("/meal/{id}")
    public ResponseEntity<Map<String, Object>> deleteMeal(@PathVariable Long id) {
        log.info("=== 식단 삭제: ID={} ===", id);
        
        try {
            if (mealLogRepository.existsById(id)) {
                mealLogRepository.deleteById(id);
                log.info("식단 삭제 완료: ID={}", id);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "식단이 삭제되었습니다"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "삭제할 식단을 찾을 수 없습니다"
                ));
            }
        } catch (Exception e) {
            log.error("식단 삭제 실패: ID={}", id, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "식단 삭제에 실패했습니다"
            ));
        }
    }

    @DeleteMapping("/workout/{id}")
    public ResponseEntity<Map<String, Object>> deleteWorkout(@PathVariable Long id) {
        log.info("=== 운동 삭제: ID={} ===", id);
        
        try {
            if (workoutLogRepository.existsById(id)) {
                workoutLogRepository.deleteById(id);
                log.info("운동 삭제 완료: ID={}", id);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "운동이 삭제되었습니다"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "삭제할 운동을 찾을 수 없습니다"
                ));
            }
        } catch (Exception e) {
            log.error("운동 삭제 실패: ID={}", id, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "운동 삭제에 실패했습니다"
            ));
        }
    }

    @DeleteMapping("/emotion/{id}")
    public ResponseEntity<Map<String, Object>> deleteEmotion(@PathVariable Long id) {
        log.info("=== 감정 삭제: ID={} ===", id);
        
        try {
            if (emotionLogRepository.existsById(id)) {
                emotionLogRepository.deleteById(id);
                log.info("감정 삭제 완료: ID={}", id);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "감정이 삭제되었습니다"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "삭제할 감정을 찾을 수 없습니다"
                ));
            }
        } catch (Exception e) {
            log.error("감정 삭제 실패: ID={}", id, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "감정 삭제에 실패했습니다"
            ));
        }
    }

    @DeleteMapping("/meals/today")
    public ResponseEntity<Map<String, Object>> deleteAllTodayMeals() {
        log.info("=== 오늘의 모든 식단 삭제 ===");
        
        try {
            Long userId = 1L;
            LocalDate today = LocalDate.now();
            
            var meals = mealLogRepository.findByUserIdAndDate(userId, today);
            int count = meals.size();
            
            if (count == 0) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "삭제할 식단이 없습니다",
                    "deletedCount", 0
                ));
            }
            
            mealLogRepository.deleteAll(meals);
            log.info("오늘의 식단 {}개 삭제 완료", count);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "오늘의 모든 식단이 삭제되었습니다",
                "deletedCount", count
            ));
            
        } catch (Exception e) {
            log.error("오늘의 식단 일괄 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "식단 삭제에 실패했습니다"
            ));
        }
    }

    @DeleteMapping("/workouts/today")
    public ResponseEntity<Map<String, Object>> deleteAllTodayWorkouts() {
        log.info("=== 오늘의 모든 운동 삭제 ===");
        
        try {
            Long userId = 1L;
            LocalDate today = LocalDate.now();
            
            var workouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            int count = workouts.size();
            
            if (count == 0) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "삭제할 운동이 없습니다",
                    "deletedCount", 0
                ));
            }
            
            workoutLogRepository.deleteAll(workouts);
            log.info("오늘의 운동 {}개 삭제 완료", count);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "오늘의 모든 운동이 삭제되었습니다",
                "deletedCount", count
            ));
            
        } catch (Exception e) {
            log.error("오늘의 운동 일괄 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "운동 삭제에 실패했습니다"
            ));
        }
    }

    @DeleteMapping("/emotions/today")
    public ResponseEntity<Map<String, Object>> deleteAllTodayEmotions() {
        log.info("=== 오늘의 모든 감정 삭제 ===");
        
        try {
            Long userId = 1L;
            LocalDate today = LocalDate.now();
            
            var emotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            int count = emotions.size();
            
            if (count == 0) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "삭제할 감정이 없습니다",
                    "deletedCount", 0
                ));
            }
            
            emotionLogRepository.deleteAll(emotions);
            log.info("오늘의 감정 {}개 삭제 완료", count);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "오늘의 모든 감정이 삭제되었습니다",
                "deletedCount", count
            ));
            
        } catch (Exception e) {
            log.error("오늘의 감정 일괄 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "감정 삭제에 실패했습니다"
            ));
        }
    }

    @DeleteMapping("/all/today")
    public ResponseEntity<Map<String, Object>> deleteAllTodayData() {
        log.info("=== 오늘의 모든 데이터 삭제 ===");
        
        try {
            Long userId = 1L;
            LocalDate today = LocalDate.now();
            
            var meals = mealLogRepository.findByUserIdAndDate(userId, today);
            var workouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            var emotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            int totalCount = meals.size() + workouts.size() + emotions.size();
            
            if (totalCount == 0) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "삭제할 데이터가 없습니다",
                    "deletedCount", 0
                ));
            }
            
            mealLogRepository.deleteAll(meals);
            workoutLogRepository.deleteAll(workouts);
            emotionLogRepository.deleteAll(emotions);
            
            log.info("오늘의 모든 데이터 {}개 삭제 완료 (식단:{}, 운동:{}, 감정:{})", 
                    totalCount, meals.size(), workouts.size(), emotions.size());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "오늘의 모든 데이터가 삭제되었습니다",
                "deletedCount", totalCount,
                "details", Map.of(
                    "meals", meals.size(),
                    "workouts", workouts.size(),
                    "emotions", emotions.size()
                )
            ));
            
        } catch (Exception e) {
            log.error("오늘의 모든 데이터 삭제 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "데이터 삭제에 실패했습니다"
            ));
        }
    }
}