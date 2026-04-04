package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.*;
import com.medicine.SwasthyaSetu.service.AuthService;
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

    @PostMapping("/doctor/login")
    public ResponseEntity<DoctorLoginResponse> doctorLogin(@RequestBody DoctorLoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/doctor/register")
    public ResponseEntity<String> registerDoctor(@RequestBody DoctorRegisterRequest request) {
        return ResponseEntity.ok(authService.registerDoctor(request));
    }
}