package com.medicine.SwasthyaSetu.interceptor;

import com.medicine.SwasthyaSetu.Entity.UserSession;
import com.medicine.SwasthyaSetu.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserSessionRepository userSessionRepository;

    public AuthInterceptor(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 1. Get Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        // 2. Extract token
        String token = authHeader.substring(7); // remove "Bearer "

        // 3. Find session
        UserSession session = userSessionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        // 4. Check active
        if (!session.isActive()) {
            throw new RuntimeException("Session inactive");
        }

        // 5. Check expiry
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            throw new RuntimeException("Session expired");
        }

        // ✅ All good → allow request
        return true;
    }
}