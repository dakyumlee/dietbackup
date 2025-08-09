package com.mydiet.repository;
import com.mydiet.model.Role;
import com.mydiet.model.EmotionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {
    
    List<EmotionLog> findByUserIdAndDate(Long userId, LocalDate date);
    
    Optional<EmotionLog> findTopByUserIdAndDateOrderByCreatedAtDesc(Long userId, LocalDate date);
    
    List<EmotionLog> findByUserIdOrderByDateDesc(Long userId);
    
    List<EmotionLog> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(e) FROM EmotionLog e WHERE e.user.id = :userId AND e.date = :date")
    long countEmotionsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT e FROM EmotionLog e WHERE e.user.id = :userId AND e.date >= :startDate ORDER BY e.date DESC, e.createdAt DESC")
    List<EmotionLog> findRecentEmotionsByUser(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT e.mood, COUNT(e) FROM EmotionLog e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate GROUP BY e.mood")
    List<Object[]> getMoodStatistics(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(e.stressLevel) FROM EmotionLog e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate AND e.stressLevel IS NOT NULL")
    Double getAverageStressLevel(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(e) FROM EmotionLog e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate AND (e.mood LIKE '%좋음%' OR e.mood = '활기참')")
    long countPositiveEmotions(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    void deleteByUserIdAndDate(Long userId, LocalDate date);
}