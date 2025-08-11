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
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByProviderId(String providerId);
    
    List<User> findByRole(String role);
    
    @Query("SELECT u FROM User u WHERE u.email = :email OR u.providerId = :providerId")
    Optional<User> findByEmailOrProviderId(@Param("email") String email, @Param("providerId") String providerId);
    
    boolean existsByEmail(String email);
    
    boolean existsByProviderId(String providerId);
}