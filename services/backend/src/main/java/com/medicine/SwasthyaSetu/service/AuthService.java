package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.Entity.Doctor;
import com.medicine.SwasthyaSetu.Entity.Hospital;
import com.medicine.SwasthyaSetu.Entity.OtpVerification;
import com.medicine.SwasthyaSetu.Entity.Patient;
import com.medicine.SwasthyaSetu.dto.DoctorLoginRequest;
import com.medicine.SwasthyaSetu.dto.DoctorLoginResponse;
import com.medicine.SwasthyaSetu.dto.DoctorRegisterRequest;
import com.medicine.SwasthyaSetu.dto.SendOtpRequest;
import com.medicine.SwasthyaSetu.dto.SendOtpResponse;
import com.medicine.SwasthyaSetu.dto.VerifyOtpRequest;
import com.medicine.SwasthyaSetu.dto.VerifyOtpResponse;
import com.medicine.SwasthyaSetu.repository.DoctorRepository;
import com.medicine.SwasthyaSetu.repository.HospitalRepository;
import com.medicine.SwasthyaSetu.repository.OtpVerificationRepository;
import com.medicine.SwasthyaSetu.repository.PatientRepository;
import com.medicine.SwasthyaSetu.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final PatientRepository patientRepository;
    private final OtpVerificationRepository otpRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(PatientRepository patientRepository,
                       OtpVerificationRepository otpRepository,
                       DoctorRepository doctorRepository,
                       HospitalRepository hospitalRepository,
                       EmailService emailService,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder) {

        this.patientRepository = patientRepository;
        this.otpRepository = otpRepository;
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

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

        String token = jwtUtil.generateToken(patient.getUhid(), "PATIENT");

        VerifyOtpResponse res = new VerifyOtpResponse();
        res.setToken(token);
        res.setPatient(patient);
        res.setMessage("Login success");

        otpRepository.delete(otp);
        return res;
    }

    public DoctorLoginResponse login(DoctorLoginRequest request) {
        Doctor doctor = doctorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        String storedPassword = doctor.getPassword();
        boolean validPassword = isBcryptHash(storedPassword)
                ? passwordEncoder.matches(request.getPassword(), storedPassword)
                : storedPassword.equals(request.getPassword());

        if (!validPassword) {
            throw new RuntimeException("Invalid password");
        }

        if (!isBcryptHash(storedPassword)) {
            doctor.setPassword(passwordEncoder.encode(request.getPassword()));
            doctorRepository.save(doctor);
        }

        DoctorLoginResponse res = new DoctorLoginResponse();
        res.setToken(jwtUtil.generateToken(String.valueOf(doctor.getId()), "DOCTOR"));
        res.setDoctorId(doctor.getId());
        res.setName(doctor.getName());
        res.setEmail(doctor.getEmail());
        res.setSpecialization(doctor.getSpecialization());
        res.setHospitalId(doctor.getHospital() != null ? doctor.getHospital().getId() : null);
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

        doctorRepository.save(doctor);
        otpRepository.delete(otp);

        return "Doctor registered successfully";
    }

    private boolean isBcryptHash(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
