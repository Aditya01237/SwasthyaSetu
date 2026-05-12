package com.medicine.SwasthyaSetu.service;
import com.medicine.SwasthyaSetu.Entity.*;
import com.medicine.SwasthyaSetu.dto.*;
import com.medicine.SwasthyaSetu.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final AiClient aiClient;

    public PatientService(
            PatientRepository patientRepository,
            OtpVerificationRepository otpVerificationRepository,
            MedicalRecordRepository medicalRecordRepository,
            AppointmentRepository appointmentRepository,
            AuditLogRepository auditLogRepository,
            AiClient aiClient
    ){
        this.patientRepository = patientRepository;
        this.otpVerificationRepository = otpVerificationRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.aiClient = aiClient;
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
        patient.setEmail(request.getEmail());
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
        response.setEmail(saved.getEmail());
        response.setAge(saved.getAge());
        response.setPhone(saved.getPhone());
        response.setGender(saved.getGender());

        return response;
    }

    public PatientDetailsResponse getPatientDetails(PatientDetailsRequest request){

        OtpVerification otp = otpVerificationRepository.findByUhid(request.getUhid()).orElseThrow(
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
        Patient patientEntity = patientRepository.findByUhid(request.getUhid()).orElseThrow(
                ()-> new RuntimeException("Patient Not Found With This Number")
        );

        // change patient to patient dto
        PatientInfoDto patientInfoDto = new PatientInfoDto();
        patientInfoDto.setUhid(patientEntity.getUhid());
        patientInfoDto.setName(patientEntity.getName());
        patientInfoDto.setEmail(patientEntity.getEmail());
        patientInfoDto.setPhone(patientEntity.getPhone());
        patientInfoDto.setAge(patientEntity.getAge());
        patientInfoDto.setGender(patientEntity.getGender());

        // Get all medical record
        List<MedicalRecord> medicalRecord = medicalRecordRepository.findByPatientId(patientEntity.getId());

        // change to medical dto
        List<MedicalRecordDTO> medicalRecordDTO = medicalRecord.stream().map(r -> {
            MedicalRecordDTO dto = new MedicalRecordDTO();
            dto.setMedicines(
                    r.getMedicines().stream().map(m -> {
                        MedicineDto md = new MedicineDto();
                        md.setName(m.getName());
                        md.setDosage(m.getDosage());
                        md.setFrequency(m.getFrequency());
                        return md;
                    }).toList()
            );
            dto.setDiagnosis(r.getDiagnosis());
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

    public List<AuditLogResponse> getAuditLogs(String uhid) {

        Patient patient = patientRepository.findByUhid(uhid)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<AuditLog> logs = auditLogRepository.findByPatientId(patient.getId());

        return logs.stream().map(log -> {
            AuditLogResponse res = new AuditLogResponse();
            res.setDoctorName(log.getDoctor().getName());
            res.setAction(log.getAction());
            res.setTimestamp(log.getTimestamp());
            return res;
        }).toList();
    }


    public MedicalRecordDTO processPrescription(String uhid, Long appointmentId, MultipartFile file) {

        // 1. Get patient
        Patient patient = patientRepository.findByUhid(uhid)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // 1.5 Get appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // 2. OCR + NLP
        PrescriptionResponse ai = aiClient.process(file);

        // 4. Create MedicalRecord
        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setAppointment(appointment);
        record.setDiagnosis(ai.getDisease());
        record.setRecordDate(LocalDateTime.now());

        // 5. Medicines
        List<Medicine> medicines = ai.getMedicines().stream().map(m -> {
            Medicine med = new Medicine();
            med.setName(m.getName());
            med.setDosage(m.getDosage());
            med.setFrequency(m.getFrequency());
            med.setMedicalRecord(record);
            return med;
        }).toList();

        record.setMedicines(medicines);

        // 6. Save
        MedicalRecord saved = medicalRecordRepository.save(record);

        // 7. Convert to DTO
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setDiagnosis(saved.getDiagnosis());
        dto.setRecordDate(saved.getRecordDate());

        dto.setMedicines(
                saved.getMedicines().stream().map(m -> {
                    MedicineDto md = new MedicineDto();
                    md.setName(m.getName());
                    md.setDosage(m.getDosage());
                    md.setFrequency(m.getFrequency());
                    return md;
                }).toList()
        );

        return dto;
    }
}
