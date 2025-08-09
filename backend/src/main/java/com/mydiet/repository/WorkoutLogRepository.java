package com.mydiet.repository;

import com.mydiet.model.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    
    List<WorkoutLog> findByUserIdAndDate(Long userId, LocalDate date);
    
    List<WorkoutLog> findByUserIdOrderByDateDesc(Long userId);
    
    long countByUserId(Long userId);
    
    long countByUserIdAndDate(Long userId, LocalDate date);
    
    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date >= :date")
    long countByUserIdAndDateAfter(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT w FROM WorkoutLog w WHERE w.user.id = :userId AND w.date >= :startDate ORDER BY w.date DESC")
    List<WorkoutLog> findRecentWorkoutsByUser(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT COALESCE(SUM(w.duration), 0) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    Integer getTotalDurationByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COALESCE(SUM(w.caloriesBurned), 0) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    Integer getTotalCaloriesBurnedByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    long countWorkoutsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    void deleteByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
