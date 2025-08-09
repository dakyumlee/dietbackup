package com.mydiet.repository;
import com.mydiet.model.Role;
import com.mydiet.model.ClaudeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaudeResponseRepository extends JpaRepository<ClaudeResponse, Long> {
    
    List<ClaudeResponse> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Optional<ClaudeResponse> findTopByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);
    
    List<ClaudeResponse> findByUserIdAndType(Long userId, String type);
    
    @Query("SELECT c FROM ClaudeResponse c WHERE c.user.id = :userId AND c.createdAt >= :startDate ORDER BY c.createdAt DESC")
    List<ClaudeResponse> findRecentResponsesByUser(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT c FROM ClaudeResponse c WHERE c.user.id = :userId AND c.type = :type AND c.createdAt >= :startDate")
    List<ClaudeResponse> findRecentResponsesByUserAndType(@Param("userId") Long userId, @Param("type") String type, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(c) FROM ClaudeResponse c WHERE c.user.id = :userId AND c.createdAt >= :startDate")
    long countResponsesByUserSince(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
}