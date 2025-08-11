package com.mydiet.repository;

import com.mydiet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findFirstByEmailOrderByIdAsc(String email);
    
    Optional<User> findFirstByEmailAndProviderOrderByIdAsc(String email, String provider);
    
    List<User> findAllByEmailOrderByIdAsc(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.email = :email ORDER BY u.createdAt ASC")
    List<User> findDuplicatesByEmail(@Param("email") String email);
}