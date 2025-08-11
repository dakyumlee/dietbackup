package com.mydiet.controller;

import com.mydiet.model.*;
import com.mydiet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final UserRepository userRepository;
    private final MealLogRepository mealLogRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final EmotionLogRepository emotionLogRepository;

    @GetMapping("/all-data")
    public ResponseEntity<Map<String, Object>> getAllData() {
        log.info("=== 관리자 대시보드 전체 데이터 조회 ===");
        
        try {
            List<User> users = userRepository.findAll();
            List<MealLog> meals = mealLogRepository.findAll();
            List<WorkoutLog> workouts = workoutLogRepository.findAll();
            List<EmotionLog> emotions = emotionLogRepository.findAll();

            log.info("데이터 개수 - Users: {}, Meals: {}, Workouts: {}, Emotions: {}", 
                    users.size(), meals.size(), workouts.size(), emotions.size());

            Map<String, Object> data = new HashMap<>();
            data.put("users", users);
            data.put("meals", meals);
            data.put("workouts", workouts);
            data.put("emotions", emotions);
            data.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(data);
            
        } catch (Exception e) {
            log.error("전체 데이터 조회 실패", e);
            
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("users", new ArrayList<>());
            emptyData.put("meals", new ArrayList<>());
            emptyData.put("workouts", new ArrayList<>());
            emptyData.put("emotions", new ArrayList<>());
            emptyData.put("error", "데이터 조회 중 오류 발생: " + e.getMessage());
            emptyData.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(emptyData);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable Long userId) {
        log.info("=== 사용자 상세 데이터 조회: userId={} ===", userId);
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            LocalDate today = LocalDate.now();
            
            List<MealLog> todayMeals = mealLogRepository.findByUserIdAndDate(userId, today);
            List<WorkoutLog> todayWorkouts = workoutLogRepository.findByUserIdAndDate(userId, today);
            List<EmotionLog> todayEmotions = emotionLogRepository.findByUserIdAndDate(userId, today);
            
            List<MealLog> allMeals = mealLogRepository.findByUser(user);
            List<WorkoutLog> allWorkouts = workoutLogRepository.findByUser(user);
            List<EmotionLog> allEmotions = emotionLogRepository.findByUser(user);

            Map<String, Object> userData = new HashMap<>();
            userData.put("user", user);
            userData.put("todayMeals", todayMeals);
            userData.put("todayWorkouts", todayWorkouts);
            userData.put("todayEmotions", todayEmotions);
            userData.put("allMeals", allMeals);
            userData.put("allWorkouts", allWorkouts);
            userData.put("allEmotions", allEmotions);
            userData.put("timestamp", LocalDateTime.now());

            log.info("사용자 데이터 조회 완료 - 오늘: 식단{}개, 운동{}개, 감정{}개", 
                    todayMeals.size(), todayWorkouts.size(), todayEmotions.size());

            return ResponseEntity.ok(userData);
            
        } catch (Exception e) {
            log.error("사용자 상세 데이터 조회 실패: userId={}", userId, e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "사용자 데이터 조회에 실패했습니다: " + e.getMessage(),
                    "userId", userId,
                    "timestamp", LocalDateTime.now()
                ));
        }
    }


    @GetMapping("/system-stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        log.info("=== 시스템 통계 조회 ===");
        
        try {
            long totalUsers = userRepository.count();
            long totalMeals = mealLogRepository.count();
            long totalWorkouts = workoutLogRepository.count();
            long totalEmotions = emotionLogRepository.count();
            
            LocalDate today = LocalDate.now();
            long todayMeals = mealLogRepository.countByDate(today);
            long todayWorkouts = workoutLogRepository.countByDate(today);
            long todayEmotions = emotionLogRepository.countByDate(today);
            
            LocalDate weekStart = today.minusDays(7);
            long weekMeals = mealLogRepository.countByDateBetween(weekStart, today);
            long weekWorkouts = workoutLogRepository.countByDateBetween(weekStart, today);
            long weekEmotions = emotionLogRepository.countByDateBetween(weekStart, today);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("totalMeals", totalMeals);
            stats.put("totalWorkouts", totalWorkouts);
            stats.put("totalEmotions", totalEmotions);
            stats.put("todayMeals", todayMeals);
            stats.put("todayWorkouts", todayWorkouts);
            stats.put("todayEmotions", todayEmotions);
            stats.put("weekMeals", weekMeals);
            stats.put("weekWorkouts", weekWorkouts);
            stats.put("weekEmotions", weekEmotions);
            stats.put("timestamp", LocalDateTime.now());

            log.info("시스템 통계 조회 완료 - 총 사용자: {}, 오늘 활동: 식단{}, 운동{}, 감정{}", 
                    totalUsers, todayMeals, todayWorkouts, todayEmotions);

            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("시스템 통계 조회 실패", e);
            
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalUsers", 0);
            defaultStats.put("totalMeals", 0);
            defaultStats.put("totalWorkouts", 0);
            defaultStats.put("totalEmotions", 0);
            defaultStats.put("todayMeals", 0);
            defaultStats.put("todayWorkouts", 0);
            defaultStats.put("todayEmotions", 0);
            defaultStats.put("weekMeals", 0);
            defaultStats.put("weekWorkouts", 0);
            defaultStats.put("weekEmotions", 0);
            defaultStats.put("error", "통계 조회에 실패했습니다: " + e.getMessage());
            defaultStats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(defaultStats);
        }
    }

    @PostMapping("/create-test-data")
    public ResponseEntity<Map<String, Object>> createTestData() {
        log.info("=== 테스트 데이터 생성 ===");
        
        try {
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "테스트 데이터를 생성하려면 먼저 사용자가 필요합니다."));
            }
            
            User testUser = users.get(0);
            LocalDate today = LocalDate.now();
            
            MealLog breakfast = MealLog.builder()
                .user(testUser)
                .description("아침 - 토스트와 커피")
                .caloriesEstimate(300)
                .date(today)
                .build();
            
            MealLog lunch = MealLog.builder()
                .user(testUser)
                .description("점심 - 불고기 덮밥")
                .caloriesEstimate(650)
                .date(today)
                .build();
            
            mealLogRepository.save(breakfast);
            mealLogRepository.save(lunch);
            
            WorkoutLog running = WorkoutLog.builder()
                .user(testUser)
                .type("러닝")
                .duration(30)
                .caloriesBurned(250)
                .date(today)
                .build();
            
            workoutLogRepository.save(running);
            
            EmotionLog emotion = EmotionLog.builder()
                .user(testUser)
                .mood("좋음")
                .note("오늘 운동을 열심히 했다!")
                .date(today)
                .build();
            
            emotionLogRepository.save(emotion);
            
            log.info("테스트 데이터 생성 완료 - 사용자: {}", testUser.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "message", "테스트 데이터가 성공적으로 생성되었습니다.",
                "user", testUser.getEmail(),
                "created", Map.of(
                    "meals", 2,
                    "workouts", 1,
                    "emotions", 1
                )
            ));
            
        } catch (Exception e) {
            log.error("테스트 데이터 생성 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "테스트 데이터 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    @DeleteMapping("/clear-all-data")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        log.warn("=== 전체 데이터 삭제 요청 ===");
        
        try {
            emotionLogRepository.deleteAll();
            workoutLogRepository.deleteAll();
            mealLogRepository.deleteAll();
            
            log.warn("모든 로그 데이터가 삭제되었습니다");
            
            return ResponseEntity.ok(Map.of(
                "message", "모든 로그 데이터가 삭제되었습니다. (사용자 계정은 유지)",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("데이터 삭제 실패", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "데이터 삭제에 실패했습니다: " + e.getMessage()));
        }
    }
}