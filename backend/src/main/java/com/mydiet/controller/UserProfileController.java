package com.mydiet.controller;

import com.mydiet.dto.InbodyDataRequest;
import com.mydiet.dto.UpdateProfileRequest;
import com.mydiet.model.InbodyData;
import com.mydiet.model.User;
import com.mydiet.repository.InbodyDataRepository;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;
    private final InbodyDataRepository inbodyDataRepository;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile(HttpSession session) {
        
        log.info("=== 사용자 프로필 조회 요청 ===");
        
        Long userId = (Long) session.getAttribute("userId");
        String userEmail = (String) session.getAttribute("userEmail");
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");
        
        log.info("세션 정보: userId={}, email={}, authenticated={}", userId, userEmail, authenticated);
        
        if (!Boolean.TRUE.equals(authenticated) || userId == null) {
            log.warn("❌ 인증되지 않은 사용자의 프로필 조회 시도");
            return ResponseEntity.status(401)
                .body(Map.of("error", "로그인이 필요합니다.", "needLogin", true));
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                log.error("❌ 사용자를 찾을 수 없음: userId={}", userId);
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("email", user.getEmail());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("role", user.getRole());
            userInfo.put("weightGoal", user.getWeightGoal());
            userInfo.put("emotionMode", user.getEmotionMode());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("updatedAt", user.getUpdatedAt());
            
            log.info("✅ 사용자 프로필 조회 성공: {}", user.getEmail());
            
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            log.error("❌ 사용자 프로필 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
        @RequestBody UpdateProfileRequest request,
        HttpSession session) {
        
        log.info("=== 프로필 업데이트 요청 ===");
        log.info("요청 데이터: {}", request);
        
        Long userId = (Long) session.getAttribute("userId");
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");
        
        if (!Boolean.TRUE.equals(authenticated) || userId == null) {
            log.warn("❌ 인증되지 않은 사용자의 프로필 업데이트 시도");
            return ResponseEntity.status(401)
                .body(Map.of("error", "로그인이 필요합니다.", "needLogin", true));
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                log.error("❌ 사용자를 찾을 수 없음: userId={}", userId);
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            
            if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
                user.setNickname(request.getNickname().trim());
                log.info("닉네임 업데이트: {}", request.getNickname());
            }
            
            if (request.getWeightGoal() != null && request.getWeightGoal() > 0) {
                user.setWeightGoal(request.getWeightGoal());
                log.info("목표 체중 업데이트: {}", request.getWeightGoal());
            }
            
            if (request.getEmotionMode() != null && !request.getEmotionMode().trim().isEmpty()) {
                String emotionMode = request.getEmotionMode().trim();
                if (emotionMode.equals("무자비") || emotionMode.equals("츤데레") || emotionMode.equals("다정함")) {
                    user.setEmotionMode(emotionMode);
                    log.info("감정 모드 업데이트: {}", emotionMode);
                }
            }
            
            user.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(user);
            
            session.setAttribute("userNickname", updatedUser.getNickname());
            
            log.info("✅ 프로필 업데이트 완료: userId={}, email={}", userId, updatedUser.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필이 성공적으로 업데이트되었습니다.");
            response.put("user", Map.of(
                "id", updatedUser.getId(),
                "email", updatedUser.getEmail(),
                "nickname", updatedUser.getNickname(),
                "weightGoal", updatedUser.getWeightGoal(),
                "emotionMode", updatedUser.getEmotionMode(),
                "updatedAt", updatedUser.getUpdatedAt()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 프로필 업데이트 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "프로필 업데이트에 실패했습니다."));
        }
    }

    @PostMapping("/body-photo")
    public ResponseEntity<Map<String, Object>> saveBodyPhoto(
        @RequestParam("file") MultipartFile file,
        HttpSession session) {
        
        log.info("=== 체형 사진 업로드 요청 ===");
        log.info("파일명: {}, 크기: {} bytes", file.getOriginalFilename(), file.getSize());
        
        Long userId = (Long) session.getAttribute("userId");
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");
        
        if (!Boolean.TRUE.equals(authenticated) || userId == null) {
            log.warn("❌ 인증되지 않은 사용자의 체형 사진 업로드 시도");
            return ResponseEntity.status(401)
                .body(Map.of("error", "로그인이 필요합니다.", "needLogin", true));
        }

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일이 비어있습니다."));
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || 
                !originalFilename.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)$")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "이미지 파일만 업로드 가능합니다. (jpg, jpeg, png, gif, webp)"));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일 크기는 5MB를 초과할 수 없습니다."));
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                log.error("❌ 사용자를 찾을 수 없음: userId={}", userId);
                return ResponseEntity.notFound().build();
            }

            String uploadDir = "uploads/body-photos/" + userId;
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String fileUrl = "/uploads/body-photos/" + userId + "/" + uniqueFilename;
            
            log.info("✅ 체형 사진 저장 완료: userId={}, 파일명={}, 경로={}", 
                    userId, uniqueFilename, filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "체형 사진이 성공적으로 업로드되었습니다.");
            response.put("data", Map.of(
                "fileName", uniqueFilename,
                "fileUrl", fileUrl,
                "fileSize", file.getSize(),
                "uploadDate", LocalDateTime.now()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 체형 사진 업로드 실패: userId={}", userId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "파일 업로드에 실패했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/inbody-data")
    public ResponseEntity<Map<String, Object>> saveInbodyData(
        @RequestBody InbodyDataRequest request,
        HttpSession session) {
        
        log.info("=== 인바디 데이터 저장 요청 ===");
        log.info("요청 데이터: {}", request);
        
        Long userId = (Long) session.getAttribute("userId");
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");
        
        if (!Boolean.TRUE.equals(authenticated) || userId == null) {
            log.warn("❌ 인증되지 않은 사용자의 인바디 데이터 저장 시도");
            return ResponseEntity.status(401)
                .body(Map.of("error", "로그인이 필요합니다.", "needLogin", true));
        }

        try {
            if (request.getWeight() == null || request.getWeight() <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "올바른 체중을 입력해주세요."));
            }

            if (request.getBodyFatPercentage() != null && 
                (request.getBodyFatPercentage() < 0 || request.getBodyFatPercentage() > 100)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "체지방률은 0-100% 사이여야 합니다."));
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                log.error("❌ 사용자를 찾을 수 없음: userId={}", userId);
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();

            InbodyData inbodyData = InbodyData.builder()
                .user(user)
                .weight(request.getWeight())
                .bodyFatPercentage(request.getBodyFatPercentage())
                .muscleMass(request.getMuscleMass())
                .bodyWaterPercentage(request.getBodyWaterPercentage())
                .basalMetabolicRate(request.getBasalMetabolicRate())
                .visceralFatLevel(request.getVisceralFatLevel())
                .notes(request.getNotes())
                .recordedAt(LocalDateTime.now())
                .build();

            InbodyData savedData = inbodyDataRepository.save(inbodyData);
            
            log.info("✅ 인바디 데이터 저장 완료: userId={}, 체중={}kg", userId, savedData.getWeight());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "인바디 데이터가 성공적으로 저장되었습니다.");
            response.put("data", Map.of(
                "id", savedData.getId(),
                "weight", savedData.getWeight(),
                "bodyFatPercentage", savedData.getBodyFatPercentage(),
                "muscleMass", savedData.getMuscleMass(),
                "bodyWaterPercentage", savedData.getBodyWaterPercentage(),
                "basalMetabolicRate", savedData.getBasalMetabolicRate(),
                "visceralFatLevel", savedData.getVisceralFatLevel(),
                "notes", savedData.getNotes(),
                "recordedAt", savedData.getRecordedAt()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 인바디 데이터 저장 실패: userId={}", userId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "인바디 데이터 저장에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/inbody-data")
    public ResponseEntity<Map<String, Object>> getInbodyData(HttpSession session) {
        
        log.info("=== 인바디 데이터 조회 요청 ===");
        
        Long userId = (Long) session.getAttribute("userId");
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");
        
        if (!Boolean.TRUE.equals(authenticated) || userId == null) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "로그인이 필요합니다.", "needLogin", true));
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            var inbodyDataList = inbodyDataRepository.findByUserOrderByRecordedAtDesc(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", inbodyDataList
            ));

        } catch (Exception e) {
            log.error("❌ 인바디 데이터 조회 실패: userId={}", userId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "인바디 데이터 조회에 실패했습니다."));
        }
    }
}