package com.example.patient_service.listener;

import com.example.patient_service.dto.QrScannedEvent;
import com.example.patient_service.entity.AuditLog;
import com.example.patient_service.entity.Patient;
import com.example.patient_service.repository.AuditLogRepository;
import com.example.patient_service.repository.PatientRepository;
import com.example.patient_service.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AuditLogListener {

    private final AuditLogRepository auditLogRepository;
    private final PatientRepository patientRepository;

    public AuditLogListener(AuditLogRepository auditLogRepository, PatientRepository patientRepository) {
        this.auditLogRepository = auditLogRepository;
        this.patientRepository = patientRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QR_AUDIT_QUEUE)
    public void handleQrScannedEvent(QrScannedEvent event) {
        System.out.println("Received QR Scanned Event: " + event);
        Patient patient = patientRepository.findById(event.getPatientId()).orElse(null);
        if(patient != null) {
            AuditLog auditLog = new AuditLog();
            auditLog.setDoctorId(event.getDoctorId());
            auditLog.setPatient(patient);
            auditLog.setAction(event.getAction());
            auditLog.setTimestamp(event.getTimestamp());
            auditLogRepository.save(auditLog);
            System.out.println("AuditLog saved asynchronously.");
        }
    }
}
