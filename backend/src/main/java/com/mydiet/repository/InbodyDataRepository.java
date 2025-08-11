package com.mydiet.repository;

import com.mydiet.model.InbodyData;
import com.mydiet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InbodyDataRepository extends JpaRepository<InbodyData, Long> {
    
    List<InbodyData> findByUserOrderByRecordedAtDesc(User user);
    
    Optional<InbodyData> findTopByUserOrderByRecordedAtDesc(User user);
    
    @Query("SELECT i FROM InbodyData i WHERE i.user = :user AND i.recordedAt BETWEEN :startDate AND :endDate ORDER BY i.recordedAt DESC")
    List<InbodyData> findByUserAndRecordedAtBetween(@Param("user") User user, 
                                                   @Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
}