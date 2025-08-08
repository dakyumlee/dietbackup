package com.mydiet.service;

import com.mydiet.model.MealLog;
import com.mydiet.model.User;
import com.mydiet.repository.MealLogRepository;
import com.mydiet.repository.UserRepository;
import com.mydiet.dto.request.MealRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MealService {
    
    private final MealLogRepository mealLogRepository;
    private final UserRepository userRepository;
    
    public MealLog saveMeal(Long userId, MealRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        MealLog meal = MealLog.builder()
                .user(user)
                .description(request.getDescription())
                .caloriesEstimate(request.getCaloriesEstimate())
                .photoUrl(request.getPhotoUrl())
                .mealType(request.getMealType())
                .protein(request.getProtein())
                .carbohydrate(request.getCarbohydrate())
                .fat(request.getFat())
                .note(request.getNote())
                .date(LocalDate.now())
                .build();
        
        return mealLogRepository.save(meal);
    }
    
    @Transactional(readOnly = true)
    public List<MealLog> getTodayMeals(Long userId) {
        return mealLogRepository.findByUserIdAndDate(userId, LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public List<MealLog> getMealsByDate(Long userId, LocalDate date) {
        return mealLogRepository.findByUserIdAndDate(userId, date);
    }
    
    @Transactional(readOnly = true)
    public List<MealLog> getRecentMeals(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return mealLogRepository.findRecentMealsByUser(userId, startDate);
    }
    
    @Transactional(readOnly = true)
    public Integer getTodayTotalCalories(Long userId) {
        return mealLogRepository.getTotalCaloriesByUserAndDate(userId, LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public long getTodayMealCount(Long userId) {
        return mealLogRepository.countMealsByUserAndDate(userId, LocalDate.now());
    }
    
    public void deleteMeal(Long mealId) {
        mealLogRepository.deleteById(mealId);
    }
    
    public void deleteTodayMeals(Long userId) {
        mealLogRepository.deleteByUserIdAndDate(userId, LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public List<MealLog> getAllMeals() {
        return mealLogRepository.findAll();
    }
}