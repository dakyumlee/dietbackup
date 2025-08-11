package com.mydiet.repository;

import com.mydiet.model.EmotionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {
    
    @Query("SELECT e FROM EmotionLog e WHERE e.user.id = :userId ORDER BY e.createdAt DESC")
    List<EmotionLog> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(e) FROM EmotionLog e WHERE e.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT e FROM EmotionLog e WHERE e.user.id = :userId AND e.date = :date ORDER BY e.createdAt DESC")
    List<EmotionLog> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT e FROM EmotionLog e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate ORDER BY e.date DESC, e.createdAt DESC")
    List<EmotionLog> findByUserIdAndDateBetween(@Param("userId") Long userId, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e FROM EmotionLog e WHERE e.user.id = :userId ORDER BY e.date DESC, e.createdAt DESC")
    List<EmotionLog> findByUserIdOrderByDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT e.mood FROM EmotionLog e WHERE e.user.id = :userId AND e.date = :date ORDER BY e.createdAt DESC")
    List<String> getLatestMoodByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}