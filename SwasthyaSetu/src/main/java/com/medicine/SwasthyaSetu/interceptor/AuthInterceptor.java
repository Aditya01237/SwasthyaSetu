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
                             Object handler) throws Exception {

        // ✅ 1. Allow preflight (CORS fix)
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return true;
        }

        // ✅ 2. Get Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return false;
        }

        // ✅ 3. Extract token
        String token = authHeader.substring(7);

        // ✅ 4. Find session
        UserSession session = userSessionRepository.findByToken(token)
                .orElse(null);

        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return false;
        }

        // ✅ 5. Check active
        if (!session.isActive()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Session inactive");
            return false;
        }

        // ✅ 6. Check expiry
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Session expired");
            return false;
        }

        // ✅ 7. Attach UHID
        request.setAttribute("uhid", session.getUhid());

        return true;
    }
}