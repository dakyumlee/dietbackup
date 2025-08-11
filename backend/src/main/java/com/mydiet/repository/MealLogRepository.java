package com.mydiet.repository;

import com.mydiet.model.MealLog;
import com.mydiet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    
    List<MealLog> findByUser(User user);
    List<MealLog> findByUserAndDate(User user, LocalDate date);
    
    @Query("SELECT m FROM MealLog m WHERE m.user.id = :userId AND m.date = :date")
    List<MealLog> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(m) FROM MealLog m WHERE m.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM MealLog m WHERE m.user.id = :userId AND m.date = :date")
    long countByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(m) FROM MealLog m WHERE m.user.id = :userId AND m.date >= :startDate")
    long countByUserIdAndDateAfter(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT m FROM MealLog m WHERE m.user.id = :userId AND m.date >= :startDate ORDER BY m.date DESC")
    List<MealLog> findRecentMealsByUser(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT SUM(m.caloriesEstimate) FROM MealLog m WHERE m.user.id = :userId AND m.date = :date")
    Long getTotalCaloriesByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(m) FROM MealLog m WHERE m.user.id = :userId AND m.date = :date")
    long countMealsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("DELETE FROM MealLog m WHERE m.user.id = :userId AND m.date = :date")
    void deleteByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    // 시스템 통계용
    @Query("SELECT COUNT(m) FROM MealLog m WHERE m.date = :date")
    long countByDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(m) FROM MealLog m WHERE m.date BETWEEN :startDate AND :endDate")
    long countByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}