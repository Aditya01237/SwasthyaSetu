package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.SendOtpRequest;
import com.medicine.SwasthyaSetu.dto.VerifyOtpRequest;
import com.medicine.SwasthyaSetu.service.AuthService;
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
    public String sendOtp(@RequestBody SendOtpRequest request){
        return authService.sendOtp(request);
    }

    // Verify otp
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestBody VerifyOtpRequest request){
        return authService.verifyOtp(request);
    }
}
