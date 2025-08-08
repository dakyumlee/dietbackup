package com.mydiet.repository;

import com.mydiet.model.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    
    List<MealLog> findByUserIdAndDate(Long userId, LocalDate date);
    
    List<MealLog> findByUserIdOrderByDateDesc(Long userId);
    
    List<MealLog> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(m.caloriesEstimate) FROM MealLog m WHERE m.user.id = :userId AND m.date = :date")
    Integer getTotalCaloriesByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(m) FROM MealLog m WHERE m.user.id = :userId AND m.date = :date")
    long countMealsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT m FROM MealLog m WHERE m.user.id = :userId AND m.date >= :startDate ORDER BY m.date DESC, m.createdAt DESC")
    List<MealLog> findRecentMealsByUser(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT m.mealType, COUNT(m) FROM MealLog m WHERE m.user.id = :userId AND m.date BETWEEN :startDate AND :endDate GROUP BY m.mealType")
    List<Object[]> getMealTypeStatistics(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    void deleteByUserIdAndDate(Long userId, LocalDate date);
}