package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.Entity.*;
import com.medicine.SwasthyaSetu.dto.*;
import com.medicine.SwasthyaSetu.repository.*;
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
    private final UserSessionRepository sessionRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final EmailService emailService;

    public AuthService(PatientRepository patientRepository,
                       OtpVerificationRepository otpRepository,
                       UserSessionRepository sessionRepository,
                       DoctorRepository doctorRepository,
                       HospitalRepository hospitalRepository,
                       EmailService emailService) {

        this.patientRepository = patientRepository;
        this.otpRepository = otpRepository;
        this.sessionRepository = sessionRepository;
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
        this.emailService = emailService;
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

        // deactivate old session
        sessionRepository.findByUhidAndIsActiveTrue(patient.getUhid())
                .ifPresent(s -> {
                    s.setActive(false);
                    sessionRepository.save(s);
                });

        // create new session
        String token = UUID.randomUUID().toString();

        UserSession session = new UserSession();
        session.setToken(token);
        session.setUhid(patient.getUhid());
        session.setRole("PATIENT");
        session.setActive(true);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        sessionRepository.save(session);

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

        String doctorId = String.valueOf(doctor.getId());

        // deactivate old session
        sessionRepository.findByUhidAndIsActiveTrue(doctorId)
                .ifPresent(s -> {
                    s.setActive(false);
                    sessionRepository.save(s);
                });

        String token = UUID.randomUUID().toString();

        UserSession session = new UserSession();
        session.setToken(token);
        session.setUhid(doctorId);
        session.setRole("DOCTOR");
        session.setActive(true);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        sessionRepository.save(session);

        DoctorLoginResponse res = new DoctorLoginResponse();
        res.setToken(token);
        res.setDoctor(doctor);

        return res;
    }

    public String registerDoctor(DoctorRegisterRequest request) {

        if (doctorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Doctor already exists");
        }

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));

        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setExperience(request.getExperience());
        doctor.setFee(request.getFee());
        doctor.setEmail(request.getEmail());
        doctor.setPassword(request.getPassword()); // later encrypt
        doctor.setHospital(hospital);

        doctorRepository.save(doctor);

        return "Doctor registered successfully";
    }
}