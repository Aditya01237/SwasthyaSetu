package com.medicine.appointment.service;

import com.medicine.appointment.dto.PatientQrAccessResponse;
import com.medicine.appointment.dto.QrScanRequest;
import com.medicine.appointment.dto.QrScanResponse;
import com.medicine.appointment.entity.Doctor;
import com.medicine.appointment.entity.QRToken;
import com.medicine.appointment.repository.DoctorRepository;
import com.medicine.appointment.repository.QrTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class QrService {

    private final QrTokenRepository qrTokenRepository;
    private final DoctorRepository doctorRepository;
    private final PatientClinicalClient patientClinicalClient;

    public QrService(QrTokenRepository qrTokenRepository,
                     DoctorRepository doctorRepository,
                     PatientClinicalClient patientClinicalClient) {
        this.qrTokenRepository = qrTokenRepository;
        this.doctorRepository = doctorRepository;
        this.patientClinicalClient = patientClinicalClient;
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

        PatientQrAccessResponse qrAccess = patientClinicalClient.recordQrAccess(
                qr.getAppointment().getId(),
                doctor.getId()
        );

        qr.setUsed(true);
        qrTokenRepository.save(qr);

        QrScanResponse response = new QrScanResponse();
        response.setStatus(qrAccess.getStatus());
        response.setMessage(qrAccess.getMessage());
        response.setRecords(qrAccess.getRecords());
        return response;
    }
}
