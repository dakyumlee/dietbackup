package com.mydiet.service;

import com.mydiet.model.EmotionLog;
import com.mydiet.model.User;
import com.mydiet.repository.EmotionLogRepository;
import com.mydiet.repository.UserRepository;
import com.mydiet.dto.request.EmotionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmotionService {
    
    private final EmotionLogRepository emotionLogRepository;
    private final UserRepository userRepository;
    
    public EmotionLog saveEmotion(Long userId, EmotionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        EmotionLog emotion = EmotionLog.builder()
                .user(user)
                .mood(request.getMood())
                .stressLevel(request.getStressLevel())
                .energyLevel(request.getEnergyLevel())
                .sleepQuality(request.getSleepQuality())
                .note(request.getNote())
                .dietFeeling(request.getDietFeeling())
                .tags(request.getTags())
                .triggers(request.getTriggers())
                .date(LocalDate.now())
                .build();
        
        return emotionLogRepository.save(emotion);
    }
    
    @Transactional(readOnly = true)
    public List<EmotionLog> getTodayEmotions(Long userId) {
        return emotionLogRepository.findByUserIdAndDate(userId, LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public Optional<EmotionLog> getTodayLatestEmotion(Long userId) {
        return emotionLogRepository.findTopByUserIdAndDateOrderByCreatedAtDesc(userId, LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public List<EmotionLog> getEmotionsByDate(Long userId, LocalDate date) {
        return emotionLogRepository.findByUserIdAndDate(userId, date);
    }
    
    @Transactional(readOnly = true)
    public List<EmotionLog> getRecentEmotions(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return emotionLogRepository.findRecentEmotionsByUser(userId, startDate);
    }
    
    @Transactional(readOnly = true)
    public long getTodayEmotionCount(Long userId) {
        return emotionLogRepository.countEmotionsByUserAndDate(userId, LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public Double getAverageStressLevel(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return emotionLogRepository.getAverageStressLevel(userId, startDate, LocalDate.now());
    }
    
    public void deleteEmotion(Long emotionId) {
        emotionLogRepository.deleteById(emotionId);
    }
    
    public void deleteTodayEmotions(Long userId) {
        emotionLogRepository.deleteByUserIdAndDate(userId, LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public List<EmotionLog> getAllEmotions() {
        return emotionLogRepository.findAll();
    }
}