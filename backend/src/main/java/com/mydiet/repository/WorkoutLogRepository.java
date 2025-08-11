package com.mydiet.repository;

import com.mydiet.model.WorkoutLog;
import com.mydiet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    
    List<WorkoutLog> findByUser(User user);
    List<WorkoutLog> findByUserAndDate(User user, LocalDate date);
    
    @Query("SELECT w FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    List<WorkoutLog> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    long countByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date >= :startDate")
    long countByUserIdAndDateAfter(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT w FROM WorkoutLog w WHERE w.user.id = :userId AND w.date >= :startDate ORDER BY w.date DESC")
    List<WorkoutLog> findRecentWorkoutsByUser(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT SUM(w.duration) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    Long getTotalDurationByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(w.caloriesBurned) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    Long getTotalCaloriesBurnedByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    long countWorkoutsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("DELETE FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    void deleteByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    // 시스템 통계용
    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.date = :date")
    long countByDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.date BETWEEN :startDate AND :endDate")
    long countByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}