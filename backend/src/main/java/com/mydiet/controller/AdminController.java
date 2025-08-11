package com.mydiet.controller;

import com.mydiet.service.AdminService;
import com.mydiet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("=== 관리자 통계 요청 ===");
        
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            log.info("관리자 통계 조회 성공");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("관리자 통계 조회 실패", e);
            return ResponseEntity.status(500).body(
                Map.of("error", "통계 조회 실패: " + e.getMessage())
            );
        }
    }

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getAllData() {
        log.info("=== 관리자 전체 데이터 요청 ===");
        
        try {
            Map<String, Object> data = adminService.getAllData();
            log.info("전체 데이터 조회 성공");
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("전체 데이터 조회 실패", e);
            return ResponseEntity.status(500).body(
                Map.of("error", "데이터 조회 실패: " + e.getMessage())
            );
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        log.info("=== 관리자 사용자 목록 요청 ===");
        
        try {
            List<Map<String, Object>> users = adminService.getAllUsers();
            log.info("사용자 목록 조회 성공: {} 명", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/users/{userId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        log.info("=== 관리자 사용자 삭제 요청: userId={} ===", userId);
        
        try {
            if (!userRepository.existsById(userId)) {
                log.warn("삭제 요청된 사용자가 존재하지 않음: userId={}", userId);
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "사용자를 찾을 수 없습니다"
                ));
            }

            log.info("사용자 관련 데이터 삭제 시작: userId={}", userId);
            
            List<com.mydiet.model.MealLog> meals = mealLogRepository.findByUserId(userId);
            int deletedMeals = meals.size();
            for (com.mydiet.model.MealLog meal : meals) {
                mealLogRepository.delete(meal);
            }
            log.info("식단 기록 {}개 삭제", deletedMeals);
            
            List<com.mydiet.model.WorkoutLog> workouts = workoutLogRepository.findByUserId(userId);
            int deletedWorkouts = workouts.size();
            for (com.mydiet.model.WorkoutLog workout : workouts) {
                workoutLogRepository.delete(workout);
            }
            log.info("운동 기록 {}개 삭제", deletedWorkouts);
            
            List<com.mydiet.model.EmotionLog> emotions = emotionLogRepository.findByUserId(userId);
            int deletedEmotions = emotions.size();
            for (com.mydiet.model.EmotionLog emotion : emotions) {
                emotionLogRepository.delete(emotion);
            }
            log.info("감정 기록 {}개 삭제", deletedEmotions);
            
            userRepository.deleteById(userId);
            
            log.info("사용자 삭제 완료: userId={}, 관련 데이터 - 식단:{}, 운동:{}, 감정:{}", 
                    userId, deletedMeals, deletedWorkouts, deletedEmotions);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "사용자가 성공적으로 삭제되었습니다",
                "deletedData", Map.of(
                    "meals", deletedMeals,
                    "workouts", deletedWorkouts,
                    "emotions", deletedEmotions
                )
            ));
            
        } catch (Exception e) {
            log.error("관리자 사용자 삭제 실패: userId={}", userId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "사용자 삭제에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable Long userId) {
        log.info("=== 관리자 사용자 상세 조회: userId={} ===", userId);
        
        try {
            Map<String, Object> userDetail = adminService.getUserDetail(userId);
            log.info("사용자 상세 조회 성공: userId={}", userId);
            return ResponseEntity.ok(userDetail);
        } catch (Exception e) {
            log.error("사용자 상세 조회 실패: userId={}", userId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "사용자 상세 조회 실패: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("=== 관리자 API 상태 확인 ===");
        
        try {
            long userCount = userRepository.count();
            
            return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "timestamp", System.currentTimeMillis(),
                "userCount", userCount
            ));
        } catch (Exception e) {
            log.error("헬스체크 실패", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "unhealthy",
                "error", e.getMessage()
            ));
        }
    }
}