package com.example.auth_service.service;

import com.example.auth_service.entity.OtpVerification;
import com.example.auth_service.dto.*;
import com.example.auth_service.repository.OtpVerificationRepository;
import com.example.auth_service.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    public AuthService(OtpVerificationRepository otpRepository,
                       EmailService emailService,
                       JwtUtil jwtUtil,
                       RestTemplate restTemplate) {

        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    // ================= PATIENT =================

    public SendOtpResponse sendOtp(SendOtpRequest request) {

        PatientDto patient = restTemplate.getForObject("http://patient-service/api/patients/uhid/" + request.getUhid(), PatientDto.class);
        if(patient == null) {
            throw new RuntimeException("Patient not found");
        }

        if (patient.getEmail() == null) {
            throw new RuntimeException("Email not found for this patient");
        }

        int otp = 100000 + new Random().nextInt(900000);

        OtpVerification entity = otpRepository.findByUhid(request.getUhid())
                .orElse(new OtpVerification());

        entity.setUhid(request.getUhid());
        entity.setPhone(patient.getPhone());
        entity.setOtp(String.valueOf(otp));
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        entity.setVerified(false);

        otpRepository.save(entity);

        emailService.sendOtpEmail(patient.getEmail(), String.valueOf(otp));

        String email = patient.getEmail();
        String[] parts = email.split("@");
        String masked = String.valueOf(parts[0].charAt(0)) + "***@" + parts[1];

        SendOtpResponse res = new SendOtpResponse();
        res.setMessage("OTP sent successfully");
        res.setMaskedEmail(masked);

        return res;
    }

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {

        OtpVerification otp = otpRepository.findByUhid(request.getUhid())
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (LocalDateTime.now().isAfter(otp.getExpiryTime())) {
            throw new RuntimeException("OTP expired");
        }

        if (!otp.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        PatientDto patient = restTemplate.getForObject("http://patient-service/api/patients/uhid/" + request.getUhid(), PatientDto.class);
        if(patient == null) {
            throw new RuntimeException("Patient not found");
        }

        String token = jwtUtil.generateToken(
                patient.getUhid(),
                "PATIENT"
        );

        VerifyOtpResponse res = new VerifyOtpResponse();
        res.setToken(token);
        res.setPatient(patient);
        res.setMessage("Login success");

        otpRepository.delete(otp);

        return res;
    }

    // ================= DOCTOR =================

    public DoctorLoginResponse login(DoctorLoginRequest request) {

        DoctorDto doctor = restTemplate.getForObject("http://provider-service/api/doctors/email/" + request.getEmail(), DoctorDto.class);
        if(doctor == null) {
             throw new RuntimeException("Doctor not found");
        }

        if (!doctor.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(
                String.valueOf(doctor.getId()),
                "DOCTOR"
        );

        DoctorLoginResponse res = new DoctorLoginResponse();
        res.setToken(token);
        res.setDoctor(doctor);

        return res;
    }

    public SendOtpResponse sendDoctorOtp(String email) {

        int otp = 100000 + new Random().nextInt(900000);

        OtpVerification entity = otpRepository.findByEmail(email)
                .orElse(new OtpVerification());

        entity.setEmail(email);
        entity.setOtp(String.valueOf(otp));
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        entity.setVerified(false);

        otpRepository.save(entity);

        emailService.sendOtpEmail(email, String.valueOf(otp));

        SendOtpResponse res = new SendOtpResponse();
        res.setMessage("OTP sent to doctor email");
        res.setMaskedEmail(email);

        return res;
    }

    public boolean verifyDoctorOtp(String email, String otpInput) {

        OtpVerification otp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (LocalDateTime.now().isAfter(otp.getExpiryTime())) {
            throw new RuntimeException("OTP expired");
        }

        if (!otp.getOtp().equals(otpInput)) {
            throw new RuntimeException("Invalid OTP");
        }

        otp.setVerified(true);
        otpRepository.save(otp);

        return true;
    }

    public String registerDoctor(DoctorRegisterRequest request) {

        OtpVerification otp = otpRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Please verify email first"));

        if (!otp.isVerified()) {
            throw new RuntimeException("Email not verified");
        }

        // Forward to provider-service
        String response = restTemplate.postForObject("http://provider-service/api/doctors/register", request, String.class);

        otpRepository.delete(otp); // cleanup

        return "Doctor registered successfully. Details: " + response;
    }
}