package com.mydiet.repository;

import com.mydiet.model.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog, Long> {
    List<MealLog> findByUserId(Long userId);
    List<MealLog> findByUserIdAndDate(Long userId, LocalDate date);
    List<MealLog> findByUserIdOrderByDateDesc(Long userId);
    List<MealLog> findByDate(LocalDate date);
    long countByUserId(Long userId);
    void deleteByUserId(Long userId);
}