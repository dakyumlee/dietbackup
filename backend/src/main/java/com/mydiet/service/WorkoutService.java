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
            .intensity(request.getIntensity())
            .caloriesBurned(request.getCaloriesBurned())
            .date(request.getDate() != null ? request.getDate() : LocalDate.now())
            .build();

        return workoutLogRepository.save(workoutLog);
    }

    public List<WorkoutLog> getTodayWorkouts(Long userId) {
        return workoutLogRepository.findByUserIdAndDate(userId, LocalDate.now());
    }

    public List<WorkoutLog> getWorkoutsByDate(Long userId, LocalDate date) {
        return workoutLogRepository.findByUserIdAndDate(userId, date);
    }

    public List<WorkoutLog> getRecentWorkouts(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return workoutLogRepository.findRecentWorkoutsByUser(userId, startDate);
    }

    public Integer getTotalDurationToday(Long userId) {
        Long duration = workoutLogRepository.getTotalDurationByUserAndDate(userId, LocalDate.now());
        return duration != null ? duration.intValue() : 0;
    }

    public Integer getTotalCaloriesBurnedToday(Long userId) {
        Long calories = workoutLogRepository.getTotalCaloriesBurnedByUserAndDate(userId, LocalDate.now());
        return calories != null ? calories.intValue() : 0;
    }

    public long getTodayWorkoutCount(Long userId) {
        return workoutLogRepository.countWorkoutsByUserAndDate(userId, LocalDate.now());
    }

    public void deleteTodayWorkouts(Long userId) {
        workoutLogRepository.deleteByUserIdAndDate(userId, LocalDate.now());
    }
}