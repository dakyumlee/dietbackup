package com.mydiet.service;

import com.mydiet.dto.EmotionRequest;
import com.mydiet.model.EmotionLog;
import com.mydiet.model.User;
import com.mydiet.repository.EmotionLogRepository;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmotionService {

    private final EmotionLogRepository emotionLogRepository;
    private final UserRepository userRepository;

    public EmotionLog saveEmotion(EmotionRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        EmotionLog emotionLog = EmotionLog.builder()
            .user(user)
            .mood(request.getMood())
            .note(request.getNote())
            .stressLevel(request.getStressLevel())
            .date(request.getDate() != null ? request.getDate() : LocalDate.now())
            .build();

        return emotionLogRepository.save(emotionLog);
    }

    public List<EmotionLog> getTodayEmotions(Long userId) {
        return emotionLogRepository.findByUserIdAndDate(userId, LocalDate.now());
    }

    public Optional<EmotionLog> getLatestEmotion(Long userId) {
        List<EmotionLog> emotions = emotionLogRepository.findTopByUserIdAndDateOrderByCreatedAtDesc(userId, LocalDate.now());
        return emotions.isEmpty() ? Optional.empty() : Optional.of(emotions.get(0));
    }

    public List<EmotionLog> getEmotionsByDate(Long userId, LocalDate date) {
        return emotionLogRepository.findByUserIdAndDate(userId, date);
    }

    public List<EmotionLog> getRecentEmotions(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return emotionLogRepository.findRecentEmotionsByUser(userId, startDate);
    }

    public long getTodayEmotionCount(Long userId) {
        return emotionLogRepository.countEmotionsByUserAndDate(userId, LocalDate.now());
    }

    public Double getAverageStressLevel(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return emotionLogRepository.getAverageStressLevel(userId, startDate, LocalDate.now());
    }

    public void deleteTodayEmotions(Long userId) {
        emotionLogRepository.deleteByUserIdAndDate(userId, LocalDate.now());
    }
}