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
    List<MealLog> findByUserId(Long userId);
    List<MealLog> findByUserIdAndDate(Long userId, LocalDate date);
    List<MealLog> findByUserIdOrderByDateDesc(Long userId);
    List<MealLog> findByDate(LocalDate date);
    long countByUserId(Long userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM MealLog m WHERE m.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}