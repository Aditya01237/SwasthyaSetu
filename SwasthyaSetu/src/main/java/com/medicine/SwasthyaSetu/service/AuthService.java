package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.Entity.*;
import com.medicine.SwasthyaSetu.dto.*;
import com.medicine.SwasthyaSetu.repository.*;
import com.medicine.SwasthyaSetu.security.JwtUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final PatientRepository patientRepository;
    private final OtpVerificationRepository otpRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    public AuthService(PatientRepository patientRepository,
                       OtpVerificationRepository otpRepository,
                       DoctorRepository doctorRepository,
                       HospitalRepository hospitalRepository,
                       EmailService emailService,
                       JwtUtil jwtUtil) {

        this.patientRepository = patientRepository;
        this.otpRepository = otpRepository;
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    // ================= PATIENT =================

    public SendOtpResponse sendOtp(SendOtpRequest request) {

        Patient patient = patientRepository.findByUhid(request.getUhid())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

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

        Patient patient = patientRepository.findByUhid(request.getUhid())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // ✅ 🔥 GENERATE JWT (NOT UUID)
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

        Doctor doctor = doctorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!doctor.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // ✅ Generate JWT instead of UUID
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

        if (doctorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Doctor already exists");
        }

        // ✅ CHECK OTP VERIFIED
        OtpVerification otp = otpRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Please verify email first"));

        if (!otp.isVerified()) {
            throw new RuntimeException("Email not verified");
        }

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));

        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setExperience(request.getExperience());
        doctor.setFee(request.getFee());
        doctor.setEmail(request.getEmail());
        doctor.setPassword(request.getPassword());
        doctor.setHospital(hospital);

        doctorRepository.save(doctor);

        otpRepository.delete(otp); // cleanup

        return "Doctor registered successfully";
    }
}