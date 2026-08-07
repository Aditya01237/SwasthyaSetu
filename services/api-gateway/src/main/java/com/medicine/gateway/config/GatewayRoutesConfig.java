package com.medicine.gateway.config;

import com.medicine.gateway.security.JwtValidationGatewayFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator swasthyaSetuRoutes(
            RouteLocatorBuilder builder,
            JwtValidationGatewayFilter jwtValidationGatewayFilter,
            @Value("${app.services.auth-url}") String authUrl,
            @Value("${app.services.hospital-url}") String hospitalUrl,
            @Value("${app.services.appointment-url}") String appointmentUrl,
            @Value("${app.services.patient-url}") String patientUrl
    ) {
        return builder.routes()
                .route("admin-login-public", route -> route
                        .path("/api/auth/admin/login")
                        .uri(authUrl))
                .route("admin-management-protected", route -> route
                        .path("/api/auth/admin/**")
                        .filters(filter -> filter.filter(jwtValidationGatewayFilter))
                        .uri(authUrl))
                .route("auth-service-public", route -> route
                        .path("/api/auth/**")
                        .uri(authUrl))
                .route("hospital-service-public", route -> route
                        .method(HttpMethod.GET)
                        .and()
                        .path("/api/hospital/**", "/api/doctor/hospital/**")
                        .uri(hospitalUrl))
                .route("hospital-service-protected", route -> route
                        .path("/api/hospital/**")
                        .filters(filter -> filter.filter(jwtValidationGatewayFilter))
                        .uri(hospitalUrl))
                .route("doctor-service-protected", route -> route
                        .path("/api/doctor/**")
                        .filters(filter -> filter.filter(jwtValidationGatewayFilter))
                        .uri(hospitalUrl))
                .route("appointment-service", route -> route
                        .path("/api/appointment/**", "/api/qr/**")
                        .filters(filter -> filter.filter(jwtValidationGatewayFilter))
                        .uri(appointmentUrl))
                .route("patient-register", route -> route
                        .path("/api/patient/register")
                        .uri(patientUrl))
                .route("patient-service", route -> route
                        .path("/api/patient/**")
                        .filters(filter -> filter.filter(jwtValidationGatewayFilter))
                        .uri(patientUrl))
                .build();
    }
}
