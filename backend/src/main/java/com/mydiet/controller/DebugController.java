package com.mydiet.controller;

import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final UserRepository userRepository;

    @GetMapping("/all-data")
    public ResponseEntity<Map<String, Object>> getAllData() {
        log.info("Admin dashboard - 전체 데이터 조회");
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            result.put("users", userRepository.findAll());
            
            result.put("meals", new ArrayList<>());
            result.put("workouts", new ArrayList<>());
            result.put("emotions", new ArrayList<>());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("전체 데이터 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "데이터 조회에 실패했습니다."));
        }
    }
}