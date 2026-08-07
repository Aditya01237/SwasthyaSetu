package com.medicine.auth.service;

import com.medicine.auth.dto.DoctorRegisterRequest;
import com.medicine.auth.dto.VerifyOtpRequest;
import com.medicine.auth.entity.Doctor;
import com.medicine.auth.entity.DoctorInvitation;
import com.medicine.auth.entity.Hospital;
import com.medicine.auth.entity.OtpVerification;
import com.medicine.auth.exception.OtpValidationException;
import com.medicine.auth.repository.DoctorInvitationRepository;
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
    @Mock private DoctorInvitationRepository doctorInvitationRepository;
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
                doctorInvitationRepository,
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
    void invitedDoctorActivationStoresBcryptAndProfileIdentity() {
        DoctorRegisterRequest request = new DoctorRegisterRequest();
        request.setEmail("doctor@example.com");
        request.setPassword("plain-secret");

        DoctorInvitation invitation = pendingInvitation("doctor@example.com");
        invitation.setDoctorId(10L);
        invitation.setHospitalId("H1");
        invitation.setName("Dr Test");
        invitation.setSpecialization("General Medicine");
        invitation.setExperience(5);
        invitation.setFee(500);

        OtpVerification otp = new OtpVerification();
        otp.setEmail(request.getEmail());
        otp.setVerified(true);

        Hospital hospital = new Hospital();
        hospital.setId("H1");

        when(doctorRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(doctorInvitationRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(invitation));
        when(otpRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(otp));
        when(hospitalRepository.findById("H1")).thenReturn(Optional.of(hospital));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> {
            Doctor doctor = invocation.getArgument(0);
            doctor.setId(99L);
            return doctor;
        });

        authService.registerDoctor(request);

        ArgumentCaptor<Doctor> doctorCaptor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(doctorCaptor.capture());
        Doctor stored = doctorCaptor.getValue();

        assertEquals(10L, stored.getProfileId());
        assertNotEquals("plain-secret", stored.getPassword());
        assertTrue(passwordEncoder.matches("plain-secret", stored.getPassword()));
        assertEquals("ACCEPTED", invitation.getStatus());
        verify(authEventPublisher).publishDoctorRegistered(stored);
    }

    @Test
    void doctorWithoutInvitationCannotRequestOtp() {
        when(doctorInvitationRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.sendDoctorOtp("unknown@example.com")
        );

        assertTrue(ex.getMessage().contains("No hospital invitation"));
        verify(authEventPublisher, never()).publishOtpRequested(any(), any());
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
        DoctorInvitation invitation = pendingInvitation("doctor@example.com");
        when(doctorInvitationRepository.findByEmail("doctor@example.com")).thenReturn(Optional.of(invitation));

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

    private DoctorInvitation pendingInvitation(String email) {
        DoctorInvitation invitation = new DoctorInvitation();
        invitation.setDoctorId(1L);
        invitation.setEmail(email);
        invitation.setHospitalId("H1");
        invitation.setStatus("PENDING");
        return invitation;
    }
}
