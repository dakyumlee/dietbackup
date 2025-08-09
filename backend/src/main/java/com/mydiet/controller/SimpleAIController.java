package com.mydiet.controller;
import com.mydiet.model.Role;
import com.mydiet.config.ClaudeApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/simple-ai")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SimpleAIController {

    private final ClaudeApiClient claudeApiClient;

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testClaude(@RequestBody Map<String, String> request) {
        log.info("=== Claude API 단순 테스트 ===");
        
        try {
            String question = request.get("question");
            if (question == null || question.trim().isEmpty()) {
                question = "안녕하세요! 간단한 인사 말씀 부탁드립니다.";
            }
            
            log.info("질문: {}", question);
            
            String response = claudeApiClient.askClaude(question);
            
            log.info("Claude 응답: {}", response);
            
            Map<String, Object> result = Map.of(
                "success", true,
                "question", question,
                "answer", response,
                "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Claude API 테스트 실패", e);
            
            Map<String, Object> errorResult = Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkStatus() {
        log.info("=== Claude API 상태 확인 ===");
        
        try {
            String testResponse = claudeApiClient.askClaude("Hello, are you working?");
            
            return ResponseEntity.ok(Map.of(
                "status", "connected",
                "testResponse", testResponse,
                "timestamp", LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("Claude API 연결 실패", e);
            
            return ResponseEntity.ok(Map.of(
                "status", "disconnected",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
            ));
        }
    }
}