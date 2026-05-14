package com.example.patient_service.service;

import com.example.patient_service.entity.*;
import com.example.patient_service.dto.*;
import com.example.patient_service.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AuditLogRepository auditLogRepository;
    private final AiClient aiClient;
    private final RestTemplate restTemplate;

    public PatientService(
            PatientRepository patientRepository,
            MedicalRecordRepository medicalRecordRepository,
            AuditLogRepository auditLogRepository,
            AiClient aiClient,
            RestTemplate restTemplate
    ){
        this.patientRepository = patientRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.auditLogRepository = auditLogRepository;
        this.aiClient = aiClient;
        this.restTemplate = restTemplate;
    }

    public PatientResponse registerPatient(PatientRegisterRequest request){

        patientRepository.findByPhone(request.getPhone()).ifPresent(
                p -> {
                    throw new IllegalArgumentException("Phone already registered");
                }
        );

        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setAge(request.getAge());
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());

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

        // Note: OTP Verification has been moved to Auth Service.
        // We assume the caller (API Gateway/JWT Filter) has already authenticated the user.

        Patient patientEntity = patientRepository.findByUhid(request.getUhid()).orElseThrow(
                ()-> new RuntimeException("Patient Not Found With This Number")
        );

        PatientInfoDto patientInfoDto = new PatientInfoDto();
        patientInfoDto.setUhid(patientEntity.getUhid());
        patientInfoDto.setName(patientEntity.getName());
        patientInfoDto.setEmail(patientEntity.getEmail());
        patientInfoDto.setPhone(patientEntity.getPhone());
        patientInfoDto.setAge(patientEntity.getAge());
        patientInfoDto.setGender(patientEntity.getGender());

        List<MedicalRecord> medicalRecord = medicalRecordRepository.findByPatientId(patientEntity.getId());

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

        // Fetch appointments via REST from appointment-service
        List<AppointmentDTO> appointmentDTO = null;
        try {
            ResponseEntity<AppointmentDTO[]> response = restTemplate.getForEntity(
                "http://appointment-service/api/appointment/patient/" + patientEntity.getId(),
                AppointmentDTO[].class
            );
            if(response.getBody() != null) {
                appointmentDTO = Arrays.asList(response.getBody());
            }
        } catch (Exception e) {
            // Service might be down or appointments not found
            appointmentDTO = List.of();
        }

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
            // In a real microservice, we'd fetch doctor name from provider-service
            // For now, we will leave it empty or map from a field
            res.setDoctorName("Doctor " + log.getDoctorId()); 
            res.setAction(log.getAction());
            res.setTimestamp(log.getTimestamp());
            return res;
        }).toList();
    }


    public MedicalRecordDTO processPrescription(String uhid, Long appointmentId, MultipartFile file) {

        Patient patient = patientRepository.findByUhid(uhid)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // We verify appointment exists via rest call
        try {
             restTemplate.getForEntity("http://appointment-service/api/appointment/" + appointmentId, Object.class);
        } catch (Exception e) {
             throw new RuntimeException("Appointment not found or invalid");
        }

        PrescriptionResponse ai = aiClient.process(file);

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setAppointmentId(appointmentId); // Store ID instead of entity reference
        record.setDiagnosis(ai.getDisease());
        record.setRecordDate(LocalDateTime.now());

        List<Medicine> medicines = ai.getMedicines().stream().map(m -> {
            Medicine med = new Medicine();
            med.setName(m.getName());
            med.setDosage(m.getDosage());
            med.setFrequency(m.getFrequency());
            med.setMedicalRecord(record);
            return med;
        }).toList();

        record.setMedicines(medicines);

        MedicalRecord saved = medicalRecordRepository.save(record);

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
    public List<MedicalRecordDTO> processQrScanAccess(Long patientId, Long doctorId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));


        List<MedicalRecord> records = medicalRecordRepository.findByPatientId(patientId);

        return records.stream().map(res -> {
            MedicalRecordDTO dto = new MedicalRecordDTO();
            dto.setDiagnosis(res.getDiagnosis());
            dto.setRecordDate(res.getRecordDate());
            dto.setMedicines(
                    res.getMedicines().stream().map(m -> {
                        MedicineDto md = new MedicineDto();
                        md.setName(m.getName());
                        md.setDosage(m.getDosage());
                        md.setFrequency(m.getFrequency());
                        return md;
                    }).toList()
            );
            return dto;
        }).toList();
    }
}
