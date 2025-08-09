package com.mydiet.repository;
import com.mydiet.model.Role;
import com.mydiet.model.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    
    List<WorkoutLog> findByUserIdAndDate(Long userId, LocalDate date);
    
    List<WorkoutLog> findByUserIdOrderByDateDesc(Long userId);
    
    List<WorkoutLog> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(w.duration) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    Integer getTotalDurationByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(w.caloriesBurned) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    Integer getTotalCaloriesBurnedByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date = :date")
    long countWorkoutsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT w FROM WorkoutLog w WHERE w.user.id = :userId AND w.date >= :startDate ORDER BY w.date DESC, w.createdAt DESC")
    List<WorkoutLog> findRecentWorkoutsByUser(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT w.category, COUNT(w), SUM(w.duration) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date BETWEEN :startDate AND :endDate GROUP BY w.category")
    List<Object[]> getWorkoutCategoryStatistics(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(DISTINCT w.date) FROM WorkoutLog w WHERE w.user.id = :userId AND w.date BETWEEN :startDate AND :endDate")
    long countWorkoutDaysByUser(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    void deleteByUserIdAndDate(Long userId, LocalDate date);
}