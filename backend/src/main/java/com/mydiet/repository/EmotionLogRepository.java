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

@Repository
public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {
    List<EmotionLog> findByUserId(Long userId);
    List<EmotionLog> findByUserIdAndDate(Long userId, LocalDate date);
    List<EmotionLog> findByUserIdOrderByDateDesc(Long userId);
    List<EmotionLog> findByDate(LocalDate date);
    long countByUserId(Long userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM EmotionLog e WHERE e.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}