package com.mydiet.repository;

import com.mydiet.model.EmotionLog;
import com.mydiet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {
    
    List<EmotionLog> findByUser(User user);
    List<EmotionLog> findByUserAndDate(User user, LocalDate date);
    
    @Query("SELECT e FROM EmotionLog e WHERE e.user.id = :userId AND e.date = :date")
    List<EmotionLog> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(e) FROM EmotionLog e WHERE e.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(e) FROM EmotionLog e WHERE e.user.id = :userId AND e.date = :date")
    long countByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(e) FROM EmotionLog e WHERE e.user.id = :userId AND e.date >= :startDate")
    long countByUserIdAndDateAfter(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT e FROM EmotionLog e WHERE e.user.id = :userId AND e.date >= :startDate ORDER BY e.date DESC")
    List<EmotionLog> findRecentEmotionsByUser(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT e FROM EmotionLog e WHERE e.user.id = :userId AND e.date = :date ORDER BY e.id DESC")
    List<EmotionLog> findTopByUserIdAndDateOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(e) FROM EmotionLog e WHERE e.user.id = :userId AND e.date = :date")
    long countEmotionsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT AVG(e.id) FROM EmotionLog e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    Double getAverageStressLevel(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("DELETE FROM EmotionLog e WHERE e.user.id = :userId AND e.date = :date")
    void deleteByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(e) FROM EmotionLog e WHERE e.date = :date")
    long countByDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(e) FROM EmotionLog e WHERE e.date BETWEEN :startDate AND :endDate")
    long countByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
