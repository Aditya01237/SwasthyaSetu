package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.CommonResponse;
import com.medicine.SwasthyaSetu.dto.SendOtpRequest;
import com.medicine.SwasthyaSetu.dto.VerifyOtpRequest;
import com.medicine.SwasthyaSetu.dto.VerifyOtpResponse;
import com.medicine.SwasthyaSetu.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin()
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
    public ResponseEntity<CommonResponse<VerifyOtpResponse>> verifyOtp(@RequestBody VerifyOtpRequest request){
        VerifyOtpResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(
                new CommonResponse<>("OTP Verified Successfully", response, 200)
        );
    }
}
