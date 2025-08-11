package com.mydiet.controller;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class CleanupController {

    private final UserRepository userRepository;

    @PostMapping("/cleanup-duplicates")
    public ResponseEntity<Map<String, Object>> cleanupDuplicateUsers() {
        log.info("=== 중복 사용자 정리 시작 ===");
        
        try {
            List<User> allUsers = userRepository.findAll();
            Map<String, List<User>> usersByEmail = allUsers.stream()
                .collect(Collectors.groupingBy(User::getEmail));
            
            int deletedCount = 0;
            
            for (Map.Entry<String, List<User>> entry : usersByEmail.entrySet()) {
                String email = entry.getKey();
                List<User> users = entry.getValue();
                
                if (users.size() > 1) {
                    log.info("중복 이메일 발견: {} ({}개)", email, users.size());
                    
                    User keepUser = users.get(0);
                    for (int i = 1; i < users.size(); i++) {
                        User deleteUser = users.get(i);
                        userRepository.delete(deleteUser);
                        deletedCount++;
                        log.info("중복 사용자 삭제: ID={}, 이메일={}", deleteUser.getId(), deleteUser.getEmail());
                    }
                    
                    log.info("유지된 사용자: ID={}, 이메일={}", keepUser.getId(), keepUser.getEmail());
                }
            }
            
            log.info("=== 중복 사용자 정리 완료: {}개 삭제 ===", deletedCount);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "중복 사용자 정리 완료",
                "deletedCount", deletedCount
            ));
            
        } catch (Exception e) {
            log.error("중복 사용자 정리 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/check-duplicates")
    public ResponseEntity<Map<String, Object>> checkDuplicates() {
        try {
            List<User> allUsers = userRepository.findAll();
            Map<String, List<User>> usersByEmail = allUsers.stream()
                .collect(Collectors.groupingBy(User::getEmail));
            
            Map<String, Integer> duplicates = usersByEmail.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().size()
                ));
            
            return ResponseEntity.ok(Map.of(
                "totalUsers", allUsers.size(),
                "duplicateEmails", duplicates,
                "duplicateCount", duplicates.size()
            ));
            
        } catch (Exception e) {
            log.error("중복 확인 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}