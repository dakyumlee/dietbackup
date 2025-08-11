package com.mydiet.repository;

import com.mydiet.model.EmotionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {
    List<EmotionLog> findByUserId(Long userId);
    List<EmotionLog> findByUserIdAndDate(Long userId, LocalDate date);
    List<EmotionLog> findByUserIdOrderByDateDesc(Long userId);
    List<EmotionLog> findByDate(LocalDate date);
    long countByUserId(Long userId);
    void deleteByUserId(Long userId);
}