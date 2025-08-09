package com.mydiet.repository;

import com.mydiet.model.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    
    List<MealLog> findByUserIdAndDate(Long userId, LocalDate date);
    
    List<MealLog> findByUserIdOrderByDateDesc(Long userId);
    
    long countByUserId(Long userId);
    
    long countByUserIdAndDate(Long userId, LocalDate date);
    
    @Query("SELECT COUNT(m) FROM MealLog m WHERE m.user.id = :userId AND m.date >= :date")
    long countByUserIdAndDateAfter(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT m FROM MealLog m WHERE m.user.id = :userId AND m.date >= :startDate ORDER BY m.date DESC")
    List<MealLog> findRecentMealsByUser(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT COALESCE(SUM(m.caloriesEstimate), 0) FROM MealLog m WHERE m.user.id = :userId AND m.date = :date")
    Integer getTotalCaloriesByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(m) FROM MealLog m WHERE m.user.id = :userId AND m.date = :date")
    long countMealsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM MealLog m WHERE m.user.id = :userId AND m.date = :date")
    void deleteByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}