package com.medicine.appointment.service;

import com.medicine.appointment.dto.MedicalRecordDTO;
import com.medicine.appointment.dto.MedicineDto;
import com.medicine.appointment.dto.QrScanRequest;
import com.medicine.appointment.dto.QrScanResponse;
import com.medicine.appointment.entity.AuditLog;
import com.medicine.appointment.entity.Doctor;
import com.medicine.appointment.entity.MedicalRecord;
import com.medicine.appointment.entity.QRToken;
import com.medicine.appointment.repository.AuditLogRepository;
import com.medicine.appointment.repository.DoctorRepository;
import com.medicine.appointment.repository.MedicalRecordRepository;
import com.medicine.appointment.repository.QrTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QrService {

    private final QrTokenRepository qrTokenRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final DoctorRepository doctorRepository;
    private final AuditLogRepository auditLogRepository;

    public QrService(QrTokenRepository qrTokenRepository,
                     MedicalRecordRepository medicalRecordRepository,
                     DoctorRepository doctorRepository,
                     AuditLogRepository auditLogRepository) {
        this.qrTokenRepository = qrTokenRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.doctorRepository = doctorRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public QrScanResponse scan(QrScanRequest request) {
        QRToken qr = qrTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid QR"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(qr.getValidFrom()) || now.isAfter(qr.getValidTo())) {
            throw new RuntimeException("QR Code Expired");
        }

        if (qr.isUsed()) {
            throw new RuntimeException("QR already used");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor Not Found"));

        List<MedicalRecordDTO> records = medicalRecordRepository.findByPatientId(qr.getPatient().getId())
                .stream()
                .map(this::toMedicalRecordDTO)
                .toList();

        qr.setUsed(true);
        qrTokenRepository.save(qr);

        AuditLog auditLog = new AuditLog();
        auditLog.setDoctor(doctor);
        auditLog.setPatient(qr.getPatient());
        auditLog.setAction("QR_SCAN");
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);

        QrScanResponse response = new QrScanResponse();
        response.setStatus("SUCCESS");
        response.setMessage("Access Granted");
        response.setRecords(records);
        return response;
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
}
