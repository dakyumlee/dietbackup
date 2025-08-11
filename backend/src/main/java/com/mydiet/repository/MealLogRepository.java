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
    
    @Query("SELECT m FROM MealLog m WHERE m.user.id = :userId ORDER BY m.createdAt DESC")
    List<MealLog> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM MealLog m WHERE m.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT m FROM MealLog m WHERE m.user.id = :userId AND m.date = :date ORDER BY m.createdAt DESC")
    List<MealLog> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT m FROM MealLog m WHERE m.user.id = :userId AND m.date BETWEEN :startDate AND :endDate ORDER BY m.date DESC, m.createdAt DESC")
    List<MealLog> findByUserIdAndDateBetween(@Param("userId") Long userId, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    @Query("SELECT m FROM MealLog m WHERE m.user.id = :userId ORDER BY m.date DESC, m.createdAt DESC")
    List<MealLog> findByUserIdOrderByDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT COALESCE(SUM(m.caloriesEstimate), 0) FROM MealLog m WHERE m.user.id = :userId AND m.date = :date")
    Integer getTotalCaloriesByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
