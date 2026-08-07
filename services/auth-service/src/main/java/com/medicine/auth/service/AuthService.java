package com.medicine.auth.service;

import com.medicine.auth.dto.DoctorLoginRequest;
import com.medicine.auth.dto.DoctorLoginResponse;
import com.medicine.auth.dto.DoctorRegisterRequest;
import com.medicine.auth.dto.SendOtpRequest;
import com.medicine.auth.dto.SendOtpResponse;
import com.medicine.auth.dto.VerifyOtpRequest;
import com.medicine.auth.dto.VerifyOtpResponse;
import com.medicine.auth.entity.Doctor;
import com.medicine.auth.entity.Hospital;
import com.medicine.auth.entity.OtpVerification;
import com.medicine.auth.entity.Patient;
import com.medicine.auth.exception.OtpValidationException;
import com.medicine.auth.repository.DoctorRepository;
import com.medicine.auth.repository.HospitalRepository;
import com.medicine.auth.repository.OtpVerificationRepository;
import com.medicine.auth.repository.PatientRepository;
import com.medicine.auth.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final OtpVerificationRepository otpRepository;
    private final AuthEventPublisher authEventPublisher;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final int maxOtpAttempts;
    private final long resendCooldownSeconds;

    public AuthService(PatientRepository patientRepository,
                       DoctorRepository doctorRepository,
                       HospitalRepository hospitalRepository,
                       OtpVerificationRepository otpRepository,
                       AuthEventPublisher authEventPublisher,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.otp.max-attempts:5}") int maxOtpAttempts,
                       @Value("${app.otp.resend-cooldown-seconds:60}") long resendCooldownSeconds) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
        this.otpRepository = otpRepository;
        this.authEventPublisher = authEventPublisher;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.maxOtpAttempts = maxOtpAttempts;
        this.resendCooldownSeconds = resendCooldownSeconds;
    }

    @Transactional
    public SendOtpResponse sendOtp(SendOtpRequest request) {
        Patient patient = patientRepository.findByUhid(request.getUhid())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (patient.getEmail() == null || patient.getEmail().isBlank()) {
            throw new RuntimeException("Email not found for this patient");
        }

        OtpVerification otp = otpRepository.findByUhid(request.getUhid())
                .orElse(new OtpVerification());
        enforceResendCooldown(otp);

        otp.setUhid(request.getUhid());
        otp.setPhone(patient.getPhone());
        otp.setEmail(null);
        otp.setOtp(generateOtp());
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otp.setVerified(false);
        otp.setAttemptCount(0);
        otp.setLastSentAt(LocalDateTime.now());
        otpRepository.save(otp);

        authEventPublisher.publishOtpRequested(patient.getEmail(), otp.getOtp());

        SendOtpResponse response = new SendOtpResponse();
        response.setMessage("OTP sent successfully");
        response.setMaskedEmail(maskEmail(patient.getEmail()));
        return response;
    }

    @Transactional(dontRollbackOn = OtpValidationException.class)
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        OtpVerification otp = otpRepository.findByUhid(request.getUhid())
                .orElseThrow(() -> new OtpValidationException("OTP not found"));

        validateOtp(otp, request.getOtp());

        Patient patient = patientRepository.findByUhid(request.getUhid())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        VerifyOtpResponse response = new VerifyOtpResponse();
        response.setToken(jwtUtil.generateToken(patient.getUhid(), "PATIENT"));
        response.setPatient(patient);
        response.setMessage("Login success");

        otpRepository.delete(otp);
        return response;
    }

    @Transactional
    public DoctorLoginResponse login(DoctorLoginRequest request) {
        Doctor doctor = doctorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        String storedPassword = doctor.getPassword();
        boolean validPassword = isBcryptHash(storedPassword)
                ? passwordEncoder.matches(request.getPassword(), storedPassword)
                : storedPassword != null && storedPassword.equals(request.getPassword());

        if (!validPassword) {
            throw new RuntimeException("Invalid password");
        }

        if (!isBcryptHash(storedPassword)) {
            doctor.setPassword(passwordEncoder.encode(request.getPassword()));
            doctorRepository.save(doctor);
        }

        DoctorLoginResponse response = new DoctorLoginResponse();
        response.setToken(jwtUtil.generateToken(String.valueOf(doctor.getId()), "DOCTOR"));
        response.setDoctor(doctor);
        return response;
    }

    @Transactional
    public SendOtpResponse sendDoctorOtp(String email) {
        OtpVerification otp = otpRepository.findByEmail(email)
                .orElse(new OtpVerification());
        enforceResendCooldown(otp);

        otp.setEmail(email);
        otp.setUhid(null);
        otp.setPhone(null);
        otp.setOtp(generateOtp());
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otp.setVerified(false);
        otp.setAttemptCount(0);
        otp.setLastSentAt(LocalDateTime.now());
        otpRepository.save(otp);

        authEventPublisher.publishOtpRequested(email, otp.getOtp());

        SendOtpResponse response = new SendOtpResponse();
        response.setMessage("OTP sent to doctor email");
        response.setMaskedEmail(maskEmail(email));
        return response;
    }

    @Transactional(dontRollbackOn = OtpValidationException.class)
    public boolean verifyDoctorOtp(String email, String otpInput) {
        OtpVerification otp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new OtpValidationException("OTP not found"));
        validateOtp(otp, otpInput);
        otp.setVerified(true);
        otpRepository.save(otp);
        return true;
    }

    @Transactional
    public String registerDoctor(DoctorRegisterRequest request) {
        if (doctorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Doctor already exists");
        }

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
        doctor.setPassword(passwordEncoder.encode(request.getPassword()));
        doctor.setHospital(hospital);
        Doctor saved = doctorRepository.save(doctor);
        authEventPublisher.publishDoctorRegistered(saved);

        otpRepository.delete(otp);
        return "Doctor registered successfully";
    }

    private void validateOtp(OtpVerification otp, String otpInput) {
        LocalDateTime now = LocalDateTime.now();
        if (otp.getExpiryTime() == null || now.isAfter(otp.getExpiryTime())) {
            otpRepository.delete(otp);
            throw new OtpValidationException("OTP expired. Request a new OTP.");
        }

        if (otp.getAttemptCount() >= maxOtpAttempts) {
            otpRepository.delete(otp);
            throw new OtpValidationException("Too many OTP attempts. Request a new OTP.");
        }

        if (!otp.getOtp().equals(otpInput)) {
            int attempts = otp.getAttemptCount() + 1;
            otp.setAttemptCount(attempts);
            if (attempts >= maxOtpAttempts) {
                otpRepository.delete(otp);
                throw new OtpValidationException("Too many OTP attempts. Request a new OTP.");
            }
            otpRepository.save(otp);
            throw new OtpValidationException(
                    "Invalid OTP. " + (maxOtpAttempts - attempts) + " attempt(s) remaining.");
        }
    }

    private void enforceResendCooldown(OtpVerification otp) {
        if (otp.getLastSentAt() == null) {
            return;
        }

        LocalDateTime allowedAt = otp.getLastSentAt().plusSeconds(resendCooldownSeconds);
        if (LocalDateTime.now().isBefore(allowedAt)) {
            long secondsRemaining = Math.max(1,
                    java.time.Duration.between(LocalDateTime.now(), allowedAt).getSeconds());
            throw new OtpValidationException(
                    "Please wait " + secondsRemaining + " second(s) before requesting another OTP.");
        }
    }

    private boolean isBcryptHash(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }

    private String generateOtp() {
        return String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));
    }

    private String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2 || parts[0].isBlank()) {
            return email;
        }
        return parts[0].charAt(0) + "***@" + parts[1];
    }
}
