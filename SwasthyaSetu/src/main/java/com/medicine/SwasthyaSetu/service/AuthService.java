package com.medicine.SwasthyaSetu.service;
import com.medicine.SwasthyaSetu.Entity.OtpVerification;
import com.medicine.SwasthyaSetu.Entity.Patient;
import com.medicine.SwasthyaSetu.Entity.UserSession;
import com.medicine.SwasthyaSetu.dto.SendOtpRequest;
import com.medicine.SwasthyaSetu.dto.VerifyOtpRequest;
import com.medicine.SwasthyaSetu.dto.VerifyOtpResponse;
import com.medicine.SwasthyaSetu.repository.OtpVerificationRepository;
import com.medicine.SwasthyaSetu.repository.PatientRepository;
import com.medicine.SwasthyaSetu.repository.UserSessionRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final PatientRepository patientRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final UserSessionRepository userSessionRepository;

    public AuthService(PatientRepository patientRepository,
                       OtpVerificationRepository otpVerificationRepository,
                       UserSessionRepository userSessionRepository
    ){
        this.patientRepository = patientRepository;
        this.otpVerificationRepository = otpVerificationRepository;
        this.userSessionRepository = userSessionRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // Send Otp
    public String sendOtp(SendOtpRequest request){

        Patient patient = patientRepository.findByUhid(request.getUhid())
                .orElseThrow(() -> new RuntimeException("Patient Not Found"));

        String phone = patient.getPhone();

        int otp = 100000 + new Random().nextInt(900000);

        // CHECK existing OTP
        OtpVerification otpEntity = otpVerificationRepository.findByUhid(request.getUhid())
                .orElse(new OtpVerification());

        // UPDATE fields
        otpEntity.setPhone(phone);
        otpEntity.setUhid(request.getUhid());
        otpEntity.setOtp(String.valueOf(otp));
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpEntity.setVerified(false);
        otpVerificationRepository.save(otpEntity);
        log.info("OTP : " + otp);

        return "OTP sent successfully";
    }

    // verify otp

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request){
        OtpVerification otp = otpVerificationRepository.findByUhid(request.getUhid()).orElseThrow(
                ()-> new RuntimeException("Otp Not Found")
        );

        // check expiry
        if(LocalDateTime.now().isAfter(otp.getExpiryTime())){
            throw new RuntimeException("OTP expired");
        }

        // check match
        if(!otp.getOtp().equals(request.getOtp())){
            throw new RuntimeException("Invalid OTP");
        }

        // check for current previous session
        userSessionRepository.findByUhidAndIsActiveTrue(request.getUhid())
                .ifPresent(session -> {
                    session.setActive(false);
                    userSessionRepository.save(session);
                });

        Patient patient = patientRepository.findByUhid(request.getUhid())
                .orElseThrow(() -> new RuntimeException("Patient Not Found"));

        // Saving Otp Token To backend
        String token_id = UUID.randomUUID().toString();
        UserSession userSession = new UserSession();
        userSession.setUhid(patient.getUhid());
        userSession.setToken(token_id);
        userSession.setCreatedAt(LocalDateTime.now());
        userSession.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        userSession.setActive(true);
        userSessionRepository.save(userSession);

        VerifyOtpResponse verifyOtpResponse = new VerifyOtpResponse();
        verifyOtpResponse.setMessage("Login successful");
        verifyOtpResponse.setToken(token_id);
        verifyOtpResponse.setPatient(patient);

        otp.setVerified(true);
        otp.setOtp(null);
        otpVerificationRepository.save(otp);

        return verifyOtpResponse;
    }
}
