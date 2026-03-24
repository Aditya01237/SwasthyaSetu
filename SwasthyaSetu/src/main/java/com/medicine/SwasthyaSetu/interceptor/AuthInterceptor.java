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
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // ✅ 1. Allow preflight (VERY IMPORTANT for CORS)
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 2. Get Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        // 3. Extract token
        String token = authHeader.substring(7);

        // 4. Find session
        UserSession session = userSessionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        // 5. Check active
        if (!session.isActive()) {
            throw new RuntimeException("Session inactive");
        }

        // 6. Check expiry
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            throw new RuntimeException("Session expired");
        }

        // ✅ 7. Attach UHID to request (VERY IMPORTANT CHANGE)
        request.setAttribute("uhid", session.getUhid());

        // allow request
        return true;
    }
}