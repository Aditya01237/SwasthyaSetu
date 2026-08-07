package com.medicine.appointment.service;

import com.medicine.appointment.dto.QrScanRequest;
import com.medicine.appointment.entity.Appointment;
import com.medicine.appointment.entity.Doctor;
import com.medicine.appointment.entity.QRToken;
import com.medicine.appointment.exception.AuthorizationException;
import com.medicine.appointment.repository.DoctorRepository;
import com.medicine.appointment.repository.QrTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QrServiceSecurityTest {

    @Mock private QrTokenRepository qrTokenRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PatientClinicalClient patientClinicalClient;

    @Test
    void doctorCannotScanQrForAnotherDoctorsAppointment() {
        QrService service = new QrService(qrTokenRepository, doctorRepository, patientClinicalClient);

        Doctor assignedDoctor = new Doctor();
        assignedDoctor.setId(2L);

        Appointment appointment = new Appointment();
        appointment.setId(50L);
        appointment.setDoctor(assignedDoctor);

        QRToken qr = new QRToken();
        qr.setToken("qr-token");
        qr.setAppointment(appointment);
        qr.setValidFrom(LocalDateTime.now().minusMinutes(5));
        qr.setValidTo(LocalDateTime.now().plusMinutes(5));
        qr.setUsed(false);

        Doctor authenticatedDoctor = new Doctor();
        authenticatedDoctor.setId(1L);

        when(qrTokenRepository.findByToken("qr-token")).thenReturn(Optional.of(qr));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(authenticatedDoctor));

        QrScanRequest request = new QrScanRequest();
        request.setToken("qr-token");

        assertThrows(AuthorizationException.class, () -> service.scan(request, 1L));

        verify(patientClinicalClient, never()).recordQrAccess(50L, 1L);
        verify(qrTokenRepository, never()).save(qr);
    }

    @Test
    void expiredQrIsRejectedBeforePatientDataAccess() {
        QrService service = new QrService(qrTokenRepository, doctorRepository, patientClinicalClient);

        QRToken qr = new QRToken();
        qr.setToken("expired-token");
        qr.setValidFrom(LocalDateTime.now().minusHours(3));
        qr.setValidTo(LocalDateTime.now().minusHours(2));
        qr.setUsed(false);

        when(qrTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(qr));

        QrScanRequest request = new QrScanRequest();
        request.setToken("expired-token");

        assertThrows(RuntimeException.class, () -> service.scan(request, 1L));

        verify(doctorRepository, never()).findById(1L);
        verify(patientClinicalClient, never()).recordQrAccess(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
    }
}
