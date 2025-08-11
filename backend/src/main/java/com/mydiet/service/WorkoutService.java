package com.mydiet.service;

import com.mydiet.dto.WorkoutRequest;
import com.mydiet.model.WorkoutLog;
import com.mydiet.model.User;
import com.mydiet.repository.WorkoutLogRepository;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutLogRepository workoutLogRepository;
    private final UserRepository userRepository;

    public WorkoutLog saveWorkout(WorkoutRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        WorkoutLog workoutLog = WorkoutLog.builder()
            .user(user)
            .type(request.getType())
            .duration(request.getDuration())
            .caloriesBurned(request.getCaloriesBurned())
            .date(request.getDate() != null ? request.getDate() : LocalDate.now())
            .build();

        return workoutLogRepository.save(workoutLog);
    }

    public List<WorkoutLog> getTodayWorkouts(Long userId) {
        return workoutLogRepository.findByUserIdAndDate(userId, LocalDate.now());
    }

    public List<WorkoutLog> getRecentWorkouts(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return workoutLogRepository.findByUserId(userId).stream()
            .filter(workout -> !workout.getDate().isBefore(startDate))
            .toList();
    }

    public long getTodayTotalDuration(Long userId) {
        return workoutLogRepository.findByUserIdAndDate(userId, LocalDate.now()).stream()
            .mapToLong(workout -> workout.getDuration() != null ? workout.getDuration() : 0)
            .sum();
    }

    public long getTodayCaloriesBurned(Long userId) {
        return workoutLogRepository.findByUserIdAndDate(userId, LocalDate.now()).stream()
            .mapToLong(workout -> workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0)
            .sum();
    }

    public long getTodayWorkoutCount(Long userId) {
        return workoutLogRepository.findByUserIdAndDate(userId, LocalDate.now()).size();
    }

    public void deleteTodayWorkouts(Long userId) {
        List<WorkoutLog> todayWorkouts = workoutLogRepository.findByUserIdAndDate(userId, LocalDate.now());
        workoutLogRepository.deleteAll(todayWorkouts);
    }
}
