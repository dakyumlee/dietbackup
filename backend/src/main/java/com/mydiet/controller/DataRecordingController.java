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
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataRecordingController {

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

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayData(HttpSession session) {
        
        log.info("=== 오늘 데이터 조회 요청 ===");
        
        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자를 찾을 수 없습니다"));
            }

            LocalDate today = LocalDate.now();
            log.info("사용자 ID: {}, 조회 날짜: {}", userId, today);
            
            List<MealLog> meals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> workouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> emotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            Map<String, Object> todayData = new HashMap<>();
            todayData.put("meals", meals);
            todayData.put("workouts", workouts);
            todayData.put("emotions", emotions);
            todayData.put("date", today);
            todayData.put("userId", userId);
            
            log.info("✅ 오늘 데이터 조회 완료: 식사 {}개, 운동 {}개, 감정 {}개", 
                    meals.size(), workouts.size(), emotions.size());
            
            return ResponseEntity.ok(todayData);
            
        } catch (Exception e) {
            log.error("❌ 오늘 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "데이터 조회에 실패했습니다: " + e.getMessage()));
        }
    }
}