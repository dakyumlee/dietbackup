package com.mydiet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserDataController {

    @GetMapping("/today-data")
    public ResponseEntity<Map<String, Object>> getTodayData(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "로그인이 필요합니다."));
            }

            LocalDate today = LocalDate.now();
            
            Map<String, Object> todayData = new HashMap<>();
            todayData.put("meals", java.util.List.of());
            todayData.put("workouts", java.util.List.of());
            todayData.put("emotions", java.util.List.of());
            todayData.put("date", today);
            
            return ResponseEntity.ok(todayData);
            
        } catch (Exception e) {
            log.error("오늘 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "데이터 조회에 실패했습니다."));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "로그인이 필요합니다."));
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCalories", 0);
            stats.put("totalExerciseTime", 0);
            stats.put("totalBurnedCalories", 0);
            stats.put("moodCount", 0);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("사용자 통계 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "통계 조회에 실패했습니다."));
        }
    }
}
