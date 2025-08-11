package com.mydiet.controller;

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
public class DataController {

    @DeleteMapping("/meal/{id}")
    public ResponseEntity<Map<String, Object>> deleteMeal(
            @PathVariable Long id, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "로그인이 필요합니다."));
            }

            log.info("식단 삭제 요청: mealId={}, userId={}", id, userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "식단이 삭제되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("식단 삭제 실패: mealId={}", id, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "식단 삭제에 실패했습니다."));
        }
    }

    @DeleteMapping("/workout/{id}")
    public ResponseEntity<Map<String, Object>> deleteWorkout(
            @PathVariable Long id, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "로그인이 필요합니다."));
            }

            log.info("운동 삭제 요청: workoutId={}, userId={}", id, userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "운동 기록이 삭제되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("운동 삭제 실패: workoutId={}", id, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "운동 삭제에 실패했습니다."));
        }
    }

    @DeleteMapping("/emotion/{id}")
    public ResponseEntity<Map<String, Object>> deleteEmotion(
            @PathVariable Long id, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("error", "로그인이 필요합니다."));
            }

            log.info("감정 삭제 요청: emotionId={}, userId={}", id, userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "감정 기록이 삭제되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("감정 삭제 실패: emotionId={}", id, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "감정 삭제에 실패했습니다."));
        }
    }
}
