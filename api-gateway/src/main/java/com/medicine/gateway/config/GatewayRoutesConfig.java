package com.medicine.gateway.config;

import com.medicine.gateway.security.JwtValidationGatewayFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator swasthyaSetuRoutes(
            RouteLocatorBuilder builder,
            JwtValidationGatewayFilter jwtValidationGatewayFilter,
            @Value("${app.services.backend-url}") String backendUrl,
            @Value("${app.services.auth-url}") String authUrl,
            @Value("${app.services.hospital-url}") String hospitalUrl,
            @Value("${app.services.appointment-url}") String appointmentUrl
    ) {
        return builder.routes()
                .route("auth-service", route -> route
                        .path("/api/auth/**")
                        .uri(authUrl))
                .route("hospital-service", route -> route
                        .path("/api/hospital/**", "/api/doctor/hospital/**")
                        .uri(hospitalUrl))
                .route("doctor-service-protected", route -> route
                        .path("/api/doctor/**")
                        .filters(filter -> filter.filter(jwtValidationGatewayFilter))
                        .uri(hospitalUrl))
                .route("appointment-service", route -> route
                        .path("/api/appointment/**", "/api/qr/**")
                        .filters(filter -> filter.filter(jwtValidationGatewayFilter))
                        .uri(appointmentUrl))
                .route("protected-monolith-transition", route -> route
                        .path("/api/patient/**")
                        .filters(filter -> filter.filter(jwtValidationGatewayFilter))
                        .uri(backendUrl))
                .build();
    }
}
