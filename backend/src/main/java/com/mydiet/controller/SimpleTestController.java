package com.mydiet.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class SimpleTestController {

    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> simpleTest() {
        log.info("간단한 테스트 API 호출");
        return ResponseEntity.ok(Map.of(
            "status", "ok", 
            "message", "API 정상 작동",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestBody Map<String, Object> request) {
        log.info("POST 요청 테스트: {}", request);
        return ResponseEntity.ok(Map.of(
            "received", request,
            "timestamp", LocalDateTime.now().toString(),
            "message", "데이터를 성공적으로 받았습니다"
        ));
    }
}