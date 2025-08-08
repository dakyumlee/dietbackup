package com.mydiet.controller;

import com.mydiet.config.ClaudeApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/claude-test")
@RequiredArgsConstructor
public class ClaudeTestController {

    private final ClaudeApiClient claudeApiClient;
    
    @Value("${claude.api.key}")
    private String apiKey;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> checkConfig() {
        return ResponseEntity.ok(Map.of(
            "apiKeyConfigured", apiKey != null && !apiKey.trim().isEmpty() && !apiKey.startsWith("${"),
            "apiKeyLength", apiKey != null ? apiKey.length() : 0,
            "apiKeyPrefix", apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) + "..." : "null"
        ));
    }

    @GetMapping("/hello")
    public ResponseEntity<String> testClaude() {
        log.info("=== Claude 테스트 시작 ===");
        String prompt = "안녕하세요! 한 문장으로 인사해주세요.";
        String response = claudeApiClient.askClaude(prompt);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/simple")
    public ResponseEntity<String> testSimple(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            question = "안녕하세요!";
        }
        
        String response = claudeApiClient.askClaude(question);
        return ResponseEntity.ok(response);
    }
}