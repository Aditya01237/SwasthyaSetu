package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.Entity.*;
import com.medicine.SwasthyaSetu.dto.QrScanRequest;
import com.medicine.SwasthyaSetu.dto.QrScanResponse;
import com.medicine.SwasthyaSetu.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QrService {

    private final QrTokenRepository qrTokenRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final DoctorRepository doctorRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserSessionRepository userSessionRepository;

    public QrService(QrTokenRepository qrTokenRepository,
                     MedicalRecordRepository medicalRecordRepository,
                     DoctorRepository doctorRepository,
                     AuditLogRepository auditLogRepository,
                     UserSessionRepository userSessionRepository
    ){
        this.qrTokenRepository = qrTokenRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.doctorRepository = doctorRepository;
        this.auditLogRepository = auditLogRepository;
        this.userSessionRepository = userSessionRepository;
    }

    public QrScanResponse scan(QrScanRequest request){

        // Find token
        QRToken qr = qrTokenRepository.findByToken(request.getToken()).orElseThrow(
                () -> new RuntimeException("Invalid QR"));

        // Check time validity
        LocalDateTime currTime = LocalDateTime.now();
        if(currTime.isBefore(qr.getValidFrom()) || currTime.isAfter(qr.getValidTo())){
            throw new RuntimeException("QR Code Expired");
        }

        // Check already used
        if(qr.isUsed()){
            throw new RuntimeException("QR already used");
        }


        // check doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId()).orElseThrow(
                ()-> new RuntimeException("Doctor Not Found")
        );

        // Fetch medical records

        List<MedicalRecord> records = medicalRecordRepository.findByPatientId(qr.getPatient().getId());

        // convert to simple list
        List<String> data = records.stream()
                .map(r -> r.getDiagnosis() + " | " + r.getPrescription())
                .collect(Collectors.toList());

        // Mark QR as used
        qr.setUsed(true);
        qrTokenRepository.save(qr);

        // Response
        QrScanResponse response = new QrScanResponse();
        response.setStatus("SUCCESS");
        response.setMessage("Access Granted");
        response.setRecords(data);

        // Audit Log
        AuditLog auditLog = new AuditLog();
        auditLog.setDoctor(doctor);
        auditLog.setPatient(qr.getPatient());
        auditLog.setAction("QR_SCAN");
        auditLog.setTimestamp(LocalDateTime.now());
        // save audit log
        auditLogRepository.save(auditLog);

        return response;
    }
}
