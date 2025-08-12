package com.mydiet.controller;

import com.mydiet.model.*;
import com.mydiet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
   
   private final MealLogRepository mealLogRepository;
   private final WorkoutLogRepository workoutLogRepository;
   private final EmotionLogRepository emotionLogRepository;
   private final UserRepository userRepository;
   
   @GetMapping("/today-data")
   public ResponseEntity<Map<String, Object>> getTodayData(HttpSession session) {
       log.info("=== 오늘 데이터 조회 요청 ===");
       
       Long userId = (Long) session.getAttribute("userId");
       if (userId == null) userId = 1L;
       
       try {
           LocalDate today = LocalDate.now();
           List<MealLog> todayMeals = mealLogRepository.findByUserIdAndDate(userId, today);
           List<WorkoutLog> todayWorkouts = workoutLogRepository.findByUserIdAndDate(userId, today);
           List<EmotionLog> todayEmotions = emotionLogRepository.findByUserIdAndDate(userId, today);
           
           Map<String, Object> response = new HashMap<>();
           response.put("success", true);
           response.put("date", today.toString());
           response.put("meals", todayMeals);
           response.put("workouts", todayWorkouts);
           response.put("emotions", todayEmotions);
           
           return ResponseEntity.ok(response);
           
       } catch (Exception e) {
           log.error("❌ 오늘 데이터 조회 실패", e);
           return ResponseEntity.internalServerError()
               .body(Map.of("error", "데이터 조회에 실패했습니다."));
       }
   }
   
   @GetMapping("/stats")
   public ResponseEntity<Map<String, Object>> getUserStats(HttpSession session) {
       log.info("=== 사용자 통계 조회 요청 ===");
       
       Long userId = (Long) session.getAttribute("userId");
       if (userId == null) userId = 1L;

       try {
           LocalDate today = LocalDate.now();
           
           int mealCount = mealLogRepository.findByUserIdAndDate(userId, today).size();
           int workoutCount = workoutLogRepository.findByUserIdAndDate(userId, today).size();
           int emotionCount = emotionLogRepository.findByUserIdAndDate(userId, today).size();
           
           int totalCalories = mealLogRepository.findByUserIdAndDate(userId, today)
               .stream()
               .mapToInt(meal -> meal.getCaloriesEstimate() != null ? meal.getCaloriesEstimate() : 0)
               .sum();
           
           int burnedCalories = workoutLogRepository.findByUserIdAndDate(userId, today)
               .stream()
               .mapToInt(workout -> workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0)
               .sum();
           
           double goalAchievement = totalCalories > 0 ? Math.min((double) totalCalories / 2000 * 100, 100) : 0;
           
           long totalMeals = mealLogRepository.countByUserId(userId);
           long totalWorkouts = workoutLogRepository.countByUserId(userId);
           long totalEmotions = emotionLogRepository.countByUserId(userId);
           
           Map<String, Object> stats = new HashMap<>();
           stats.put("mealCount", mealCount);
           stats.put("workoutCount", workoutCount);
           stats.put("emotionCount", emotionCount);
           stats.put("totalCalories", totalCalories);
           stats.put("burnedCalories", burnedCalories);
           stats.put("goalAchievement", Math.round(goalAchievement));
           stats.put("totalMeals", totalMeals);
           stats.put("totalWorkouts", totalWorkouts);
           stats.put("totalEmotions", totalEmotions);
           stats.put("accountAge", "신규 사용자");
           stats.put("lastActivity", "오늘");
           
           return ResponseEntity.ok(stats);
           
       } catch (Exception e) {
           log.error("❌ 사용자 통계 조회 실패", e);
           return ResponseEntity.internalServerError()
               .body(Map.of("error", "통계 조회에 실패했습니다."));
       }
   }

   @GetMapping("/profile")
   public ResponseEntity<Map<String, Object>> getCurrentUserProfile(HttpSession session) {
       log.info("=== 사용자 프로필 조회 요청 ===");
       
       Long userId = (Long) session.getAttribute("userId");
       if (userId == null) userId = 1L;
       
       log.info("세션 userId: {}", userId);

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
           userInfo.put("currentWeight", user.getCurrentWeight());
           userInfo.put("height", user.getHeight());
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
   public ResponseEntity<Map<String, Object>> updateCurrentUserProfile(
       @RequestBody Map<String, Object> profileData,
       HttpServletRequest request) {
       
       log.info("=== 프로필 업데이트 요청 ===");
       log.info("요청 데이터: {}", profileData);
       
       try {
           HttpSession session = request.getSession(false);
           Long userId = null;
           
           if (session != null) {
               userId = (Long) session.getAttribute("userId");
               log.info("세션에서 userId 추출: {}", userId);
           }
           
           if (userId == null) {
               userId = 1L;
               log.info("세션에 userId가 없어서 기본값 사용: {}", userId);
           }
           
           Optional<User> userOpt = userRepository.findById(userId);
           if (userOpt.isEmpty()) {
               log.error("사용자를 찾을 수 없음: userId={}", userId);
               return ResponseEntity.notFound().build();
           }
           
           User user = userOpt.get();
           log.info("사용자 찾음: {} ({})", user.getNickname(), user.getEmail());
           
           if (profileData.containsKey("nickname")) {
               String nickname = (String) profileData.get("nickname");
               user.setNickname(nickname);
               log.info("닉네임 업데이트: {}", nickname);
           }
           
           if (profileData.containsKey("currentWeight")) {
               Object currentWeight = profileData.get("currentWeight");
               if (currentWeight != null) {
                   user.setCurrentWeight(((Number) currentWeight).doubleValue());
                   log.info("현재 체중 업데이트: {}", user.getCurrentWeight());
               }
           }
           
           if (profileData.containsKey("height")) {
               Object height = profileData.get("height");
               if (height != null) {
                   user.setHeight(((Number) height).doubleValue());
                   log.info("키 업데이트: {}", user.getHeight());
               }
           }
           
           if (profileData.containsKey("weightGoal")) {
               Object weightGoal = profileData.get("weightGoal");
               if (weightGoal != null) {
                   user.setWeightGoal(((Number) weightGoal).doubleValue());
                   log.info("목표 체중 업데이트: {}", user.getWeightGoal());
               }
           }
           
           if (profileData.containsKey("emotionMode")) {
               String emotionMode = (String) profileData.get("emotionMode");
               user.setEmotionMode(emotionMode);
               log.info("감정 모드 업데이트: {}", emotionMode);
           }
           
           user.setUpdatedAt(LocalDateTime.now());
           User updatedUser = userRepository.save(user);
           log.info("✅ 프로필 저장 완료: ID={}", updatedUser.getId());
           
           return ResponseEntity.ok(Map.of(
               "success", true,
               "message", "프로필이 성공적으로 업데이트되었습니다.",
               "user", updatedUser
           ));
           
       } catch (Exception e) {
           log.error("프로필 업데이트 실패", e);
           return ResponseEntity.internalServerError()
               .body(Map.of("error", "프로필 업데이트에 실패했습니다."));
       }
   }
}