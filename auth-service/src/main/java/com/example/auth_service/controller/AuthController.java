package com.example.auth_service.controller;

import com.example.auth_service.dto.*;
import com.example.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    public String getProfile(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        return "User ID: " + userId;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<CommonResponse<SendOtpResponse>> sendOtp(@RequestBody SendOtpRequest request) {
        SendOtpResponse res = authService.sendOtp(request);
        return ResponseEntity.ok(
                new CommonResponse<>("OTP sent", res, 200)
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<CommonResponse<VerifyOtpResponse>> verifyOtp(@RequestBody VerifyOtpRequest req) {
        return ResponseEntity.ok(
                new CommonResponse<>("Login success", authService.verifyOtp(req), 200)
        );
    }

    @PostMapping("/doctor/send-otp")
    public ResponseEntity<?> sendDoctorOtp(@RequestParam String email) {
        return ResponseEntity.ok(authService.sendDoctorOtp(email));
    }

    @PostMapping("/doctor/verify-otp")
    public ResponseEntity<?> verifyDoctorOtp(@RequestParam String email,
                                             @RequestParam String otp) {
        authService.verifyDoctorOtp(email, otp);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/doctor/login")
    public ResponseEntity<DoctorLoginResponse> doctorLogin(@RequestBody DoctorLoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/doctor/register")
    public ResponseEntity<String> registerDoctor(@RequestBody DoctorRegisterRequest request) {
        return ResponseEntity.ok(authService.registerDoctor(request));
    }
}