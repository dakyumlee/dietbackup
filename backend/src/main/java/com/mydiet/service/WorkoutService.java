package com.mydiet.service;

import com.mydiet.model.WorkoutLog;
import com.mydiet.model.User;
import com.mydiet.repository.WorkoutLogRepository;
import com.mydiet.repository.UserRepository;
import com.mydiet.dto.request.WorkoutRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WorkoutService {
    
    private final WorkoutLogRepository workoutLogRepository;
    private final UserRepository userRepository;
    
    public WorkoutLog saveWorkout(Long userId, WorkoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        WorkoutLog workout = WorkoutLog.builder()
                .user(user)
                .type(request.getType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .intensity(request.getIntensity())
                .category(request.getCategory())
                .sets(request.getSets())
                .distance(request.getDistance())
                .note(request.getNote())
                .date(LocalDate.now())
                .build();
        
        return workoutLogRepository.save(workout);
    }
    
    @Transactional(readOnly = true)
    public List<WorkoutLog> getTodayWorkouts(Long userId) {
        return workoutLogRepository.findByUserIdAndDate(userId, LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public List<WorkoutLog> getWorkoutsByDate(Long userId, LocalDate date) {
        return workoutLogRepository.findByUserIdAndDate(userId, date);
    }
    
    @Transactional(readOnly = true)
    public List<WorkoutLog> getRecentWorkouts(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return workoutLogRepository.findRecentWorkoutsByUser(userId, startDate);
    }
    
    @Transactional(readOnly = true)
    public Integer getTodayTotalDuration(Long userId) {
        return workoutLogRepository.getTotalDurationByUserAndDate(userId, LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public Integer getTodayTotalCaloriesBurned(Long userId) {
        return workoutLogRepository.getTotalCaloriesBurnedByUserAndDate(userId, LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public long getTodayWorkoutCount(Long userId) {
        return workoutLogRepository.countWorkoutsByUserAndDate(userId, LocalDate.now());
    }
    
    public void deleteWorkout(Long workoutId) {
        workoutLogRepository.deleteById(workoutId);
    }
    
    public void deleteTodayWorkouts(Long userId) {
        workoutLogRepository.deleteByUserIdAndDate(userId, LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public List<WorkoutLog> getAllWorkouts() {
        return workoutLogRepository.findAll();
    }
}