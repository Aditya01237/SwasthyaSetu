package com.medicine.SwasthyaSetu.service;
import com.medicine.SwasthyaSetu.Entity.OtpVerification;
import com.medicine.SwasthyaSetu.Entity.Patient;
import com.medicine.SwasthyaSetu.dto.SendOtpRequest;
import com.medicine.SwasthyaSetu.dto.VerifyOtpRequest;
import com.medicine.SwasthyaSetu.repository.OtpVerificationRepository;
import com.medicine.SwasthyaSetu.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final PatientRepository patientRepository;
    private final OtpVerificationRepository otpVerificationRepository;

    public AuthService(PatientRepository patientRepository, OtpVerificationRepository otpVerificationRepository){
        this.patientRepository = patientRepository;
        this.otpVerificationRepository = otpVerificationRepository;
    }

    // Send Otp
    public String sendOtp(SendOtpRequest request){

        Patient patient = patientRepository.findByUhid(request.getUhid()).orElseThrow(
                ()-> new RuntimeException("Patient Not Found")
        );

        String phone = patient.getPhone();

        int otp = 100000 + new Random().nextInt(900000);

        OtpVerification otpEntity = new OtpVerification();
        otpEntity.setPhone(patient.getPhone());
        otpEntity.setOtp(String.valueOf(otp));
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpEntity.setVerified(false);

        otpVerificationRepository.save(otpEntity);

        System.out.println("OTP : " + otp);

        return "OTP sent successfully";
    }

    // verify otp

    public String verifyOtp(VerifyOtpRequest request){
        OtpVerification otp = otpVerificationRepository.findByPhone(request.getPhone()).orElseThrow(
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

        otp.setVerified(true);
        otpVerificationRepository.save(otp);

        return "OTP verified successfully";
    }
}
