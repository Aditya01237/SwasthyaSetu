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

        String uri = request.getRequestURI();

        // ✅ Allow public routes
        if (uri.startsWith("/api/auth") || uri.contains("/login") || uri.contains("/register")) {
            return true;
        }

        // ✅ Get Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(response, "Missing or invalid Authorization header");
        }

        // ✅ Extract token
        String token = authHeader.substring(7);

        // ✅ Find session — NULL CHECK FIRST
        UserSession session = userSessionRepository.findByToken(token).orElse(null);

        if (session == null) {
            return unauthorized(response, "Invalid token");
        }

        // ✅ Check active
        if (!session.isActive()) {
            return unauthorized(response, "Session inactive");
        }

        // ✅ Check expiry
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            return unauthorized(response, "Session expired");
        }

        // ✅ Attach user info
        request.setAttribute("userId", session.getUhid());
        request.setAttribute("role", session.getRole());

        // ✅ Role-based access — SINGLE CHECK, GET allowed for all
        if (uri.startsWith("/api/doctor") && !"DOCTOR".equalsIgnoreCase(session.getRole())) {
            if (!request.getMethod().equalsIgnoreCase("GET")) {
                return forbidden(response, "Access denied: Doctor only");
            }
        }

        if (uri.startsWith("/api/patient") && !"PATIENT".equalsIgnoreCase(session.getRole())) {
            if (!request.getMethod().equalsIgnoreCase("GET")) {
                return forbidden(response, "Access denied: Patient only");
            }
        }

        return true;
    }

    // 🔥 Unauthorized
    private boolean unauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
        return false;
    }

    // 🔥 Forbidden
    private boolean forbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
        return false;
    }
}