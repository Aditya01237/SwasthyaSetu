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
            @Value("${app.services.backend-url}") String backendUrl
    ) {
        return builder.routes()
                .route("auth-service-transition", route -> route
                        .path("/api/auth/**")
                        .uri(backendUrl))
                .route("hospital-service-transition", route -> route
                        .path("/api/hospital/**", "/api/doctor/hospital/**")
                        .uri(backendUrl))
                .route("protected-monolith-transition", route -> route
                        .path("/api/patient/**", "/api/appointment/**", "/api/qr/**", "/api/doctor/**")
                        .filters(filter -> filter.filter(jwtValidationGatewayFilter))
                        .uri(backendUrl))
                .build();
    }
}
