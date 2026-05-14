package com.medicine.patient.service;

import com.medicine.patient.dto.AppointmentDTO;
import com.medicine.patient.dto.AuditLogResponse;
import com.medicine.patient.dto.MedicalRecordDTO;
import com.medicine.patient.dto.MedicineDto;
import com.medicine.patient.dto.PatientDetailsResponse;
import com.medicine.patient.dto.PatientInfoDto;
import com.medicine.patient.dto.PatientRegisterRequest;
import com.medicine.patient.dto.PatientResponse;
import com.medicine.patient.dto.PrescriptionResponse;
import com.medicine.patient.client.AppointmentReadModelClient;
import com.medicine.patient.dto.AppointmentReadModelSnapshot;
import com.medicine.patient.dto.QrAccessResponse;
import com.medicine.patient.entity.Appointment;
import com.medicine.patient.entity.AuditLog;
import com.medicine.patient.entity.Doctor;
import com.medicine.patient.entity.Hospital;
import com.medicine.patient.entity.MedicalRecord;
import com.medicine.patient.entity.Medicine;
import com.medicine.patient.entity.Patient;
import com.medicine.patient.repository.AppointmentRepository;
import com.medicine.patient.repository.AuditLogRepository;
import com.medicine.patient.repository.DoctorRepository;
import com.medicine.patient.repository.HospitalRepository;
import com.medicine.patient.repository.MedicalRecordRepository;
import com.medicine.patient.repository.PatientRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final EntityManager entityManager;
    private final AppointmentReadModelClient appointmentReadModelClient;
    private final AiClient aiClient;
    private final PatientEventPublisher patientEventPublisher;

    public PatientService(PatientRepository patientRepository,
                          MedicalRecordRepository medicalRecordRepository,
                          AppointmentRepository appointmentRepository,
                          AuditLogRepository auditLogRepository,
                          DoctorRepository doctorRepository,
                          HospitalRepository hospitalRepository,
                          EntityManager entityManager,
                          AppointmentReadModelClient appointmentReadModelClient,
                          AiClient aiClient,
                          PatientEventPublisher patientEventPublisher) {
        this.patientRepository = patientRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
        this.entityManager = entityManager;
        this.appointmentReadModelClient = appointmentReadModelClient;
        this.aiClient = aiClient;
        this.patientEventPublisher = patientEventPublisher;
    }

    @Transactional
    public PatientResponse registerPatient(PatientRegisterRequest request) {
        patientRepository.findByPhone(request.getPhone()).ifPresent(patient -> {
            throw new IllegalArgumentException("Phone already registered");
        });

        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setAge(request.getAge());
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());
        patient.setUhid("UHID" + System.currentTimeMillis());
        patient.setCreatedAt(LocalDateTime.now());

        Patient saved = patientRepository.save(patient);
        patientEventPublisher.publishPatientRegistered(saved);
        return toPatientResponse(saved);
    }

    @Transactional(readOnly = true)
    public PatientDetailsResponse getPatientDetails(String uhid) {
        Patient patient = patientRepository.findByUhid(uhid)
                .orElseThrow(() -> new RuntimeException("Patient Not Found With This Number"));

        PatientDetailsResponse response = new PatientDetailsResponse();
        response.setPatient(toPatientInfo(patient));
        response.setMedicalRecord(
                medicalRecordRepository.findByPatientId(patient.getId())
                        .stream()
                        .map(this::toMedicalRecordDTO)
                        .toList()
        );
        response.setAppointment(
                appointmentRepository.findByPatientId(patient.getId())
                        .stream()
                        .map(this::toAppointmentDTO)
                        .toList()
        );
        return response;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogs(String uhid) {
        Patient patient = patientRepository.findByUhid(uhid)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        return auditLogRepository.findByPatientId(patient.getId())
                .stream()
                .map(this::toAuditLogResponse)
                .toList();
    }

    @Transactional
    public MedicalRecordDTO processPrescription(String uhid, Long appointmentId, MultipartFile file) {
        Patient patient = patientRepository.findByUhid(uhid)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getPatient() == null || !Objects.equals(appointment.getPatient().getId(), patient.getId())) {
            throw new RuntimeException("Unauthorized appointment access");
        }

        if (!auditLogRepository.existsByPatientIdAndAppointmentIdAndAction(patient.getId(), appointmentId, "QR_SCAN")) {
            throw new RuntimeException("Prescription upload locked until QR is scanned");
        }

        medicalRecordRepository.findByAppointmentId(appointmentId).ifPresent(record -> {
            throw new RuntimeException("Prescription already uploaded for this appointment");
        });

        PrescriptionResponse ai = aiClient.process(file);

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setAppointment(appointment);
        record.setDiagnosis(ai.getDisease());
        record.setRecordDate(LocalDateTime.now());

        List<Medicine> medicines = List.of();
        if (ai.getMedicines() != null) {
            medicines = ai.getMedicines().stream().map(medicineDto -> {
                Medicine medicine = new Medicine();
                medicine.setName(medicineDto.getName());
                medicine.setDosage(medicineDto.getDosage());
                medicine.setFrequency(medicineDto.getFrequency());
                medicine.setMedicalRecord(record);
                return medicine;
            }).toList();
        }

        record.setMedicines(medicines);
        return toMedicalRecordDTO(medicalRecordRepository.save(record));
    }

    @Transactional
    public QrAccessResponse recordQrAccess(Long appointmentId, Long doctorId) {
        ensureAppointmentReadModel(appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        Patient patient = appointment.getPatient();
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!Objects.equals(appointment.getDoctor().getId(), doctorId)) {
            throw new RuntimeException("Unauthorized appointment access");
        }

        AuditLog auditLog = new AuditLog();
        auditLog.setDoctor(doctor);
        auditLog.setPatient(patient);
        auditLog.setAppointment(appointment);
        auditLog.setAction("QR_SCAN");
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        QrAccessResponse response = new QrAccessResponse();
        response.setStatus("SUCCESS");
        response.setMessage("Access Granted");
        response.setRecords(
                medicalRecordRepository.findByPatientId(patient.getId())
                        .stream()
                        .map(this::toMedicalRecordDTO)
                        .toList()
        );
        return response;
    }

    /**
     * Ensures a read-model row exists (Rabbit may have been missed or patient PKs differ across services).
     */
    private void ensureAppointmentReadModel(Long appointmentId) {
        if (appointmentRepository.existsById(appointmentId)) {
            return;
        }
        AppointmentReadModelSnapshot snap = appointmentReadModelClient.fetchSnapshot(appointmentId);
        Patient patient = patientRepository.findById(snap.sourcePatientId())
                .or(() -> patientRepository.findByUhid(snap.patientUhid()))
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Doctor doctor = doctorRepository.findById(snap.doctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        Hospital hospital = hospitalRepository.findById(snap.hospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));

        Appointment appointment = new Appointment();
        appointment.setId(snap.appointmentId());
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setHospital(hospital);
        appointment.setAppointmentTime(LocalDateTime.parse(snap.appointmentTime()));
        appointment.setCreatedAt(
                snap.createdAt() == null || snap.createdAt().isBlank()
                        ? LocalDateTime.now()
                        : LocalDateTime.parse(snap.createdAt())
        );
        entityManager.persist(appointment);
    }

    @Transactional(readOnly = true)
    public Optional<MedicalRecordDTO> getMedicalRecordForAppointment(Long appointmentId) {
        return medicalRecordRepository.findByAppointmentId(appointmentId)
                .map(this::toMedicalRecordDTO);
    }

    private PatientResponse toPatientResponse(Patient patient) {
        PatientResponse response = new PatientResponse();
        response.setUhid(patient.getUhid());
        response.setName(patient.getName());
        response.setEmail(patient.getEmail());
        response.setAge(patient.getAge());
        response.setPhone(patient.getPhone());
        response.setGender(patient.getGender());
        return response;
    }

    private PatientInfoDto toPatientInfo(Patient patient) {
        PatientInfoDto dto = new PatientInfoDto();
        dto.setUhid(patient.getUhid());
        dto.setName(patient.getName());
        dto.setEmail(patient.getEmail());
        dto.setAge(patient.getAge());
        dto.setPhone(patient.getPhone());
        dto.setGender(patient.getGender());
        return dto;
    }

    private MedicalRecordDTO toMedicalRecordDTO(MedicalRecord record) {
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setDiagnosis(record.getDiagnosis());
        dto.setRecordDate(record.getRecordDate());
        dto.setMedicines(record.getMedicines().stream().map(medicine -> {
            MedicineDto medicineDto = new MedicineDto();
            medicineDto.setName(medicine.getName());
            medicineDto.setDosage(medicine.getDosage());
            medicineDto.setFrequency(medicine.getFrequency());
            return medicineDto;
        }).toList());
        return dto;
    }

    private AppointmentDTO toAppointmentDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setHospital(appointment.getHospital());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        return dto;
    }

    private AuditLogResponse toAuditLogResponse(AuditLog log) {
        AuditLogResponse response = new AuditLogResponse();
        response.setDoctorName(log.getDoctor().getName());
        response.setAction(log.getAction());
        response.setTimestamp(log.getTimestamp());
        return response;
    }
}
