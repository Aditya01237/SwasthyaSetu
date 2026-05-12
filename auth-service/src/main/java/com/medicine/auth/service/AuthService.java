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
import com.medicine.auth.repository.DoctorRepository;
import com.medicine.auth.repository.HospitalRepository;
import com.medicine.auth.repository.OtpVerificationRepository;
import com.medicine.auth.repository.PatientRepository;
import com.medicine.auth.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    public AuthService(PatientRepository patientRepository,
                       DoctorRepository doctorRepository,
                       HospitalRepository hospitalRepository,
                       OtpVerificationRepository otpRepository,
                       EmailService emailService,
                       JwtUtil jwtUtil) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
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
        otp.setUhid(request.getUhid());
        otp.setPhone(patient.getPhone());
        otp.setEmail(null);
        otp.setOtp(generateOtp());
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otp.setVerified(false);
        otpRepository.save(otp);

        emailService.sendOtpEmail(patient.getEmail(), otp.getOtp());

        SendOtpResponse response = new SendOtpResponse();
        response.setMessage("OTP sent successfully");
        response.setMaskedEmail(maskEmail(patient.getEmail()));
        return response;
    }

    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        OtpVerification otp = otpRepository.findByUhid(request.getUhid())
                .orElseThrow(() -> new RuntimeException("OTP not found"));

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

    public DoctorLoginResponse login(DoctorLoginRequest request) {
        Doctor doctor = doctorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!doctor.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
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
        otp.setEmail(email);
        otp.setUhid(null);
        otp.setPhone(null);
        otp.setOtp(generateOtp());
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otp.setVerified(false);
        otpRepository.save(otp);

        emailService.sendOtpEmail(email, otp.getOtp());

        SendOtpResponse response = new SendOtpResponse();
        response.setMessage("OTP sent to doctor email");
        response.setMaskedEmail(email);
        return response;
    }

    @Transactional
    public boolean verifyDoctorOtp(String email, String otpInput) {
        OtpVerification otp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));
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
        doctor.setPassword(request.getPassword());
        doctor.setHospital(hospital);
        doctorRepository.save(doctor);

        otpRepository.delete(otp);
        return "Doctor registered successfully";
    }

    private void validateOtp(OtpVerification otp, String otpInput) {
        if (LocalDateTime.now().isAfter(otp.getExpiryTime())) {
            throw new RuntimeException("OTP expired");
        }
        if (!otp.getOtp().equals(otpInput)) {
            throw new RuntimeException("Invalid OTP");
        }
    }

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2 || parts[0].isBlank()) {
            return email;
        }
        return parts[0].charAt(0) + "***@" + parts[1];
    }
}
