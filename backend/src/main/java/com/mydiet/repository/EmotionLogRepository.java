package com.mydiet.repository;

import com.mydiet.model.EmotionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {
    
    List<EmotionLog> findByUserIdAndDate(Long userId, LocalDate date);
    
    List<EmotionLog> findByUserIdOrderByDateDesc(Long userId);
    
    long countByUserId(Long userId);
    
    long countByUserIdAndDate(Long userId, LocalDate date);
    
    @Query("SELECT COUNT(e) FROM EmotionLog e WHERE e.user.id = :userId AND e.date >= :date")
    long countByUserIdAndDateAfter(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT e FROM EmotionLog e WHERE e.user.id = :userId AND e.date >= :startDate ORDER BY e.date DESC")
    List<EmotionLog> findRecentEmotionsByUser(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT e FROM EmotionLog e WHERE e.user.id = :userId AND e.date = :date ORDER BY e.id DESC")
    Optional<EmotionLog> findTopByUserIdAndDateOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(e) FROM EmotionLog e WHERE e.user.id = :userId AND e.date = :date")
    long countEmotionsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT AVG(CASE " +
           "WHEN e.mood = '매우 나쁨' THEN 5 " +
           "WHEN e.mood = '나쁨' THEN 4 " +
           "WHEN e.mood = '스트레스' THEN 4 " +
           "WHEN e.mood = '보통' THEN 3 " +
           "WHEN e.mood = '좋음' THEN 2 " +
           "WHEN e.mood = '매우 좋음' THEN 1 " +
           "WHEN e.mood = '활기참' THEN 1 " +
           "ELSE 3 END) FROM EmotionLog e " +
           "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    Double getAverageStressLevel(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM EmotionLog e WHERE e.user.id = :userId AND e.date = :date")
    void deleteByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}