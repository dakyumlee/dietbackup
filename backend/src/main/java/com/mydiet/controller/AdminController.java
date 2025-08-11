package com.mydiet.controller;

import com.mydiet.service.AdminService;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("관리자 통계 조회 실패", e);
            return ResponseEntity.status(500).body(
                Map.of("error", "통계 조회 실패: " + e.getMessage())
            );
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            List<Map<String, Object>> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        log.info("=== 관리자 사용자 삭제: userId={} ===", userId);
        
        try {
            if (!userRepository.existsById(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "사용자를 찾을 수 없습니다"
                ));
            }

            userRepository.deleteById(userId);
            
            log.info("관리자에 의한 사용자 삭제 완료: userId={}", userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "사용자가 성공적으로 삭제되었습니다"
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
        try {
            Map<String, Object> userDetail = adminService.getUserDetail(userId);
            return ResponseEntity.ok(userDetail);
        } catch (Exception e) {
            log.error("사용자 상세 조회 실패: userId={}", userId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "사용자 상세 조회 실패: " + e.getMessage()
            ));
        }
    }
}