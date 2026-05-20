package com.medicine.SwasthyaSetu.config;

import com.medicine.SwasthyaSetu.security.JwtFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            JwtFilter jwtFilter,
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174}") String allowedOrigins) {
        this.jwtFilter = jwtFilter;
        this.allowedOrigins = List.of(allowedOrigins.split(","))
                .stream()
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/hospital/**", "/api/doctor/hospital/**").permitAll()
                        // Patients can view doctor profiles (read-only)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/doctor/*").authenticated()
                        // Doctor appointment endpoints — require DOCTOR role
                        .requestMatchers("/api/appointment/doctor/**").hasRole("DOCTOR")
                        // Patient appointment endpoints — require PATIENT role
                        .requestMatchers("/api/appointment/**").hasRole("PATIENT")
                        // Doctor management endpoints require DOCTOR role
                        .requestMatchers("/api/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/api/patient/**").hasRole("PATIENT")
                        .anyRequest().authenticated())

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
