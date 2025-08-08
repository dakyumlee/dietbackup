package com.mydiet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        return ResponseEntity.ok(Map.of(
            "message", "Hello! API가 정상 작동합니다!",
            "status", "SUCCESS",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}