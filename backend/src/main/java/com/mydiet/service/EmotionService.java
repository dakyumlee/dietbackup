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
            .date(request.getDate() != null ? request.getDate() : LocalDate.now())
            .build();

        return emotionLogRepository.save(emotionLog);
    }

    public List<EmotionLog> getTodayEmotions(Long userId) {
        return emotionLogRepository.findByUserIdAndDate(userId, LocalDate.now());
    }

    public List<EmotionLog> getLatestEmotion(Long userId) {
        List<EmotionLog> emotions = emotionLogRepository.findByUserIdAndDate(userId, LocalDate.now());
        return emotions.isEmpty() ? List.of() : emotions.subList(0, Math.min(1, emotions.size()));
    }

    public List<EmotionLog> getRecentEmotions(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return emotionLogRepository.findByUserId(userId).stream()
            .filter(emotion -> !emotion.getDate().isBefore(startDate))
            .toList();
    }

    public long getTodayEmotionCount(Long userId) {
        return emotionLogRepository.findByUserIdAndDate(userId, LocalDate.now()).size();
    }

    public void deleteTodayEmotions(Long userId) {
        List<EmotionLog> todayEmotions = emotionLogRepository.findByUserIdAndDate(userId, LocalDate.now());
        emotionLogRepository.deleteAll(todayEmotions);
    }
}