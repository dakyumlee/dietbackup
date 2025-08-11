package com.mydiet.util;

import com.mydiet.model.User;
import com.mydiet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Component
@RequiredArgsConstructor
public class SessionUtil {

    private final UserRepository userRepository;

    public Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (Long) session.getAttribute("userId");
        }
        return null;
    }

    public User getCurrentUser(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId != null) {
            return userRepository.findById(userId).orElse(null);
        }
        return null;
    }

    public boolean isLoggedIn(HttpServletRequest request) {
        return getCurrentUserId(request) != null;
    }
}