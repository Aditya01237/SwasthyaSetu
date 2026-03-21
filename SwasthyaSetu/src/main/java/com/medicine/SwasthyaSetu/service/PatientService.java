package com.medicine.SwasthyaSetu.service;
import com.medicine.SwasthyaSetu.Entity.Appointment;
import com.medicine.SwasthyaSetu.Entity.MedicalRecord;
import com.medicine.SwasthyaSetu.Entity.OtpVerification;
import com.medicine.SwasthyaSetu.Entity.Patient;
import com.medicine.SwasthyaSetu.dto.*;
import com.medicine.SwasthyaSetu.repository.AppointmentRepository;
import com.medicine.SwasthyaSetu.repository.MedicalRecordRepository;
import com.medicine.SwasthyaSetu.repository.OtpVerificationRepository;
import com.medicine.SwasthyaSetu.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientService(PatientRepository patientRepository,
                          OtpVerificationRepository otpVerificationRepository,
                          MedicalRecordRepository medicalRecordRepository,
                          AppointmentRepository appointmentRepository
    ){
        this.patientRepository = patientRepository;
        this.otpVerificationRepository = otpVerificationRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public PatientResponse registerPatient(PatientRegisterRequest request){

        // check duplicate phone
        patientRepository.findByPhone(request.getPhone()).ifPresent(
                p -> {
                    throw new IllegalArgumentException("Phone already registered");
                }
        );

        // create entity
        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setAge(request.getAge());
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());

        // create uhid
        String uhid = "UHID" + System.currentTimeMillis();
        patient.setUhid(uhid);
        patient.setCreatedAt(LocalDateTime.now());

        Patient saved = patientRepository.save(patient);

        PatientResponse response = new PatientResponse();
        response.setUhid(saved.getUhid());
        response.setName(saved.getName());
        response.setAge(saved.getAge());
        response.setPhone(saved.getPhone());
        response.setGender(saved.getGender());

        return response;
    }

    public PatientDetailsResponse getPatientDetails(PatientDetailsRequest request){

        OtpVerification otp = otpVerificationRepository.findByPhone(request.getPhone()).orElseThrow(
                ()-> new RuntimeException("Otp Not Found")
        );

        // check expiry
        if(LocalDateTime.now().isAfter(otp.getExpiryTime())){
            throw new RuntimeException("OTP expired");
        }

        // check otp verified
        if(!otp.isVerified()){
            throw new RuntimeException("User Not Verified");
        }

        // get patient details
        Patient patientEntity = patientRepository.findByPhone(request.getPhone()).orElseThrow(
                ()-> new RuntimeException("Patient Not Found With This Number")
        );

        // change patient to patient dto
        PatientInfoDto patientInfoDto = new PatientInfoDto();
        patientInfoDto.setUhid(patientEntity.getUhid());
        patientInfoDto.setName(patientEntity.getName());
        patientInfoDto.setPhone(patientEntity.getPhone());
        patientInfoDto.setAge(patientEntity.getAge());
        patientInfoDto.setGender(patientEntity.getGender());

        // Get all medical record
        List<MedicalRecord> medicalRecord = medicalRecordRepository.findByPatientId(patientEntity.getId());

        // change to medical dto
        List<MedicalRecordDTO> medicalRecordDTO = medicalRecord.stream().map(r -> {
            MedicalRecordDTO dto = new MedicalRecordDTO();
            dto.setDiagnosis(r.getDiagnosis());
            dto.setPrescription(r.getPrescription());
            dto.setRecordDate(r.getRecordDate());
            return dto;
        }).toList();

        List<Appointment> appointments = appointmentRepository.findByPatientId(patientEntity.getId());

        List<AppointmentDTO> appointmentDTO = appointments.stream().map(r-> {
            AppointmentDTO dto = new AppointmentDTO();
            dto.setHospital(r.getHospital());
            dto.setAppointmentTime(r.getAppointmentTime());
            return dto;
        }).toList();


        PatientDetailsResponse patientDetailsResponse = new PatientDetailsResponse();
        patientDetailsResponse.setPatient(patientInfoDto);
        patientDetailsResponse.setAppointment(appointmentDTO);
        patientDetailsResponse.setMedicalRecord(medicalRecordDTO);

        return patientDetailsResponse;
    }
}
