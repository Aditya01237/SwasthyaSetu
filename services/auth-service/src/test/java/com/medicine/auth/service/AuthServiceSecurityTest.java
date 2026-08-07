package com.medicine.auth.service;

import com.medicine.auth.dto.DoctorRegisterRequest;
import com.medicine.auth.dto.VerifyOtpRequest;
import com.medicine.auth.entity.Doctor;
import com.medicine.auth.entity.Hospital;
import com.medicine.auth.entity.OtpVerification;
import com.medicine.auth.exception.OtpValidationException;
import com.medicine.auth.repository.DoctorRepository;
import com.medicine.auth.repository.HospitalRepository;
import com.medicine.auth.repository.OtpVerificationRepository;
import com.medicine.auth.repository.PatientRepository;
import com.medicine.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceSecurityTest {

    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private HospitalRepository hospitalRepository;
    @Mock private OtpVerificationRepository otpRepository;
    @Mock private AuthEventPublisher authEventPublisher;
    @Mock private JwtUtil jwtUtil;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(
                patientRepository,
                doctorRepository,
                hospitalRepository,
                otpRepository,
                authEventPublisher,
                jwtUtil,
                passwordEncoder,
                5,
                60
        );
    }

    @Test
    void registerDoctorStoresBcryptInsteadOfPlaintext() {
        DoctorRegisterRequest request = new DoctorRegisterRequest();
        request.setName("Dr Test");
        request.setSpecialization("General Medicine");
        request.setExperience(5);
        request.setFee(500);
        request.setEmail("doctor@example.com");
        request.setPassword("plain-secret");
        request.setHospitalId("H1");

        OtpVerification otp = new OtpVerification();
        otp.setEmail(request.getEmail());
        otp.setVerified(true);

        Hospital hospital = new Hospital();
        hospital.setId("H1");

        when(doctorRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(otpRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(otp));
        when(hospitalRepository.findById("H1")).thenReturn(Optional.of(hospital));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> {
            Doctor doctor = invocation.getArgument(0);
            doctor.setId(10L);
            return doctor;
        });

        authService.registerDoctor(request);

        ArgumentCaptor<Doctor> doctorCaptor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(doctorCaptor.capture());
        String storedPassword = doctorCaptor.getValue().getPassword();

        assertNotEquals("plain-secret", storedPassword);
        assertTrue(passwordEncoder.matches("plain-secret", storedPassword));
        verify(authEventPublisher).publishDoctorRegistered(doctorCaptor.getValue());
    }

    @Test
    void wrongOtpIncrementsAttemptCounter() {
        OtpVerification otp = new OtpVerification();
        otp.setUhid("UHID-1");
        otp.setOtp("123456");
        otp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otp.setAttemptCount(0);

        when(otpRepository.findByUhid("UHID-1")).thenReturn(Optional.of(otp));

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setUhid("UHID-1");
        request.setOtp("000000");

        OtpValidationException ex = assertThrows(
                OtpValidationException.class,
                () -> authService.verifyOtp(request)
        );

        assertEquals(1, otp.getAttemptCount());
        assertTrue(ex.getMessage().contains("4 attempt"));
        verify(otpRepository).save(otp);
        verify(patientRepository, never()).findByUhid("UHID-1");
    }

    @Test
    void resendCooldownBlocksImmediateDoctorOtp() {
        OtpVerification existing = new OtpVerification();
        existing.setEmail("doctor@example.com");
        existing.setLastSentAt(LocalDateTime.now());

        when(otpRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(existing));

        assertThrows(
                OtpValidationException.class,
                () -> authService.sendDoctorOtp("doctor@example.com")
        );

        verify(authEventPublisher, never()).publishOtpRequested(any(), any());
    }
}
