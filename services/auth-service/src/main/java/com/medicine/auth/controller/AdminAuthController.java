package com.medicine.auth.controller;

import com.medicine.auth.dto.AdminLoginRequest;
import com.medicine.auth.dto.AdminLoginResponse;
import com.medicine.auth.dto.CreateHospitalAdminRequest;
import com.medicine.auth.service.AdminAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth/admin")
public class AdminAuthController {

    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }

    @PostMapping("/hospital-admin")
    public ResponseEntity<Long> createHospitalAdmin(
            @RequestHeader(USER_ROLE_HEADER) String role,
            @RequestBody CreateHospitalAdminRequest request) {
        if (!AdminAuthService.ROLE_ADMIN.equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ADMIN access required");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminAuthService.createHospitalAdmin(request));
    }
}
