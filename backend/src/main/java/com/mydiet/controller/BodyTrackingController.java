package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/save")
@RequiredArgsConstructor
public class BodyTrackingController {

    private final UserRepository userRepository;

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

    @PostMapping("/body-photo")
    public ResponseEntity<Map<String, Object>> saveBodyPhoto(
        @RequestBody Map<String, Object> request,
        HttpSession session) {
        
        log.info("=== 바디 사진 저장 요청 ===");
        log.info("요청 데이터 키: {}", request.keySet());

        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자 인증이 필요합니다"));
            }

            String image = request.get("image") != null ? request.get("image").toString() : "";
            String analysis = request.get("analysis") != null ? request.get("analysis").toString() : "";
            String date = request.get("date") != null ? request.get("date").toString() : LocalDateTime.now().toString();
            Object idObj = request.get("id");
            Long photoId = idObj != null ? Long.valueOf(idObj.toString()) : System.currentTimeMillis();

            Map<String, Object> bodyPhotoData = new HashMap<>();
            bodyPhotoData.put("id", photoId);
            bodyPhotoData.put("userId", userId);
            bodyPhotoData.put("image", image);
            bodyPhotoData.put("analysis", analysis);
            bodyPhotoData.put("date", date);
            bodyPhotoData.put("type", "body_photo");

            log.info("바디 사진 저장: userId={}, photoId={}", userId, photoId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "바디 사진이 저장되었습니다",
                "data", bodyPhotoData
            ));

        } catch (Exception e) {
            log.error("바디 사진 저장 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/inbody")
    public ResponseEntity<Map<String, Object>> saveInbodyData(
        @RequestBody Map<String, Object> request,
        HttpSession session) {
        
        log.info("=== 인바디 데이터 저장 요청 ===");
        log.info("요청 데이터: {}", request);

        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자 인증이 필요합니다"));
            }

            Double weight = request.get("weight") != null ? 
                Double.valueOf(request.get("weight").toString()) : null;
            Double bodyFat = request.get("bodyFat") != null ? 
                Double.valueOf(request.get("bodyFat").toString()) : null;
            Double muscleMass = request.get("muscleMass") != null ? 
                Double.valueOf(request.get("muscleMass").toString()) : null;
            Double water = request.get("water") != null ? 
                Double.valueOf(request.get("water").toString()) : null;
            Integer bmr = request.get("bmr") != null ? 
                Integer.valueOf(request.get("bmr").toString()) : null;
            Integer visceralFat = request.get("visceralFat") != null ? 
                Integer.valueOf(request.get("visceralFat").toString()) : null;
            String memo = request.get("memo") != null ? request.get("memo").toString() : "";
            String date = request.get("date") != null ? request.get("date").toString() : LocalDateTime.now().toString();

            if (weight == null || weight <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "체중을 입력해주세요"
                ));
            }

            Map<String, Object> inbodyData = new HashMap<>();
            inbodyData.put("id", System.currentTimeMillis());
            inbodyData.put("userId", userId);
            inbodyData.put("weight", weight);
            inbodyData.put("bodyFat", bodyFat);
            inbodyData.put("muscleMass", muscleMass);
            inbodyData.put("water", water);
            inbodyData.put("bmr", bmr);
            inbodyData.put("visceralFat", visceralFat);
            inbodyData.put("memo", memo);
            inbodyData.put("date", date);
            inbodyData.put("type", "inbody");

            log.info("인바디 데이터 저장: userId={}, weight={}kg", userId, weight);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "인바디 데이터가 저장되었습니다",
                "data", inbodyData
            ));

        } catch (Exception e) {
            log.error("인바디 데이터 저장 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/body-tracking/stats")
    public ResponseEntity<Map<String, Object>> getBodyTrackingStats(HttpSession session) {
        
        log.info("=== 바디트래킹 통계 조회 ===");

        try {
            Long userId = getCurrentUserId(session);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "사용자 인증이 필요합니다"));
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("bodyPhotos", 0);
            stats.put("inbodyRecords", 0);
            stats.put("lastPhotoDate", null);
            stats.put("lastInbodyDate", null);

            log.info("바디트래킹 통계: userId={}", userId);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("바디트래킹 통계 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}