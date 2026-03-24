package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.CommonResponse;
import com.medicine.SwasthyaSetu.dto.SendOtpRequest;
import com.medicine.SwasthyaSetu.dto.VerifyOtpRequest;
import com.medicine.SwasthyaSetu.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    // send otp
    @PostMapping("/send-otp")
    public ResponseEntity<CommonResponse<String>> sendOtp(@RequestBody SendOtpRequest request){
        String response = authService.sendOtp(request);
        return ResponseEntity.ok(
                new CommonResponse<>("OTP Sent Successfully", response, 200)
        );
    }

    // Verify otp
    @PostMapping("/verify-otp")
    public ResponseEntity<CommonResponse<String>> verifyOtp(@RequestBody VerifyOtpRequest request){
        String response = authService.verifyOtp(request);
        return ResponseEntity.ok(
                new CommonResponse<>("OTP Verified Successfully", response, 200)
        );
    }
}
