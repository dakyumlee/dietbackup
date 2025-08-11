package com.mydiet.repository;

import com.mydiet.model.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    
    @Query("SELECT w FROM WorkoutLog w WHERE w.user.id = :userId ORDER BY w.createdAt DESC")
    List<WorkoutLog> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT w FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date ORDER BY w.createdAt DESC")
    List<WorkoutLog> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT w FROM WorkoutLog w WHERE w.user.id = :userId AND w.date BETWEEN :startDate AND :endDate ORDER BY w.date DESC, w.createdAt DESC")
    List<WorkoutLog> findByUserIdAndDateBetween(@Param("userId") Long userId, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    @Query("SELECT w FROM WorkoutLog w WHERE w.user.id = :userId ORDER BY w.date DESC, w.createdAt DESC")
    List<WorkoutLog> findByUserIdOrderByDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT COALESCE(SUM(w.caloriesBurned), 0) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    Integer getTotalCaloriesBurnedByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
