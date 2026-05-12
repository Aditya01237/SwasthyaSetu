package com.medicine.auth.controller;

import com.medicine.auth.dto.CommonResponse;
import com.medicine.auth.dto.DoctorLoginRequest;
import com.medicine.auth.dto.DoctorLoginResponse;
import com.medicine.auth.dto.DoctorRegisterRequest;
import com.medicine.auth.dto.SendOtpRequest;
import com.medicine.auth.dto.SendOtpResponse;
import com.medicine.auth.dto.VerifyOtpRequest;
import com.medicine.auth.dto.VerifyOtpResponse;
import com.medicine.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    public String getProfile() {
        return "Auth service is running";
    }

    @PostMapping("/send-otp")
    public ResponseEntity<CommonResponse<SendOtpResponse>> sendOtp(@RequestBody SendOtpRequest request) {
        SendOtpResponse response = authService.sendOtp(request);
        return ResponseEntity.ok(new CommonResponse<>("OTP sent", response, 200));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<CommonResponse<VerifyOtpResponse>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(new CommonResponse<>("Login success", authService.verifyOtp(request), 200));
    }

    @PostMapping("/doctor/send-otp")
    public ResponseEntity<SendOtpResponse> sendDoctorOtp(@RequestParam String email) {
        return ResponseEntity.ok(authService.sendDoctorOtp(email));
    }

    @PostMapping("/doctor/verify-otp")
    public ResponseEntity<String> verifyDoctorOtp(@RequestParam String email, @RequestParam String otp) {
        authService.verifyDoctorOtp(email, otp);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/doctor/login")
    public ResponseEntity<DoctorLoginResponse> doctorLogin(@RequestBody DoctorLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/doctor/register")
    public ResponseEntity<String> registerDoctor(@RequestBody DoctorRegisterRequest request) {
        return ResponseEntity.ok(authService.registerDoctor(request));
    }
}
