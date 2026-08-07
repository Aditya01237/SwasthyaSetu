package com.medicine.appointment.service;

import com.medicine.appointment.dto.AppointmentRequest;
import com.medicine.appointment.entity.Appointment;
import com.medicine.appointment.entity.Doctor;
import com.medicine.appointment.entity.Hospital;
import com.medicine.appointment.entity.Patient;
import com.medicine.appointment.entity.QRToken;
import com.medicine.appointment.exception.AuthorizationException;
import com.medicine.appointment.repository.AppointmentRepository;
import com.medicine.appointment.repository.DoctorRepository;
import com.medicine.appointment.repository.HospitalRepository;
import com.medicine.appointment.repository.PatientRepository;
import com.medicine.appointment.repository.QrTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentSecurityTest {

    @Mock private PatientRepository patientRepository;
    @Mock private HospitalRepository hospitalRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private QrTokenRepository qrTokenRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private AppointmentEventPublisher appointmentEventPublisher;
    @Mock private SlotLockService slotLockService;
    @Mock private PatientClinicalClient patientClinicalClient;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                patientRepository,
                hospitalRepository,
                appointmentRepository,
                qrTokenRepository,
                doctorRepository,
                appointmentEventPublisher,
                slotLockService,
                patientClinicalClient
        );
    }

    @Test
    void patientCannotReadAnotherPatientsAppointment() {
        Patient owner = new Patient();
        owner.setUhid("UHID-OWNER");

        Appointment appointment = new Appointment();
        appointment.setId(42L);
        appointment.setPatient(owner);

        when(appointmentRepository.findById(42L)).thenReturn(Optional.of(appointment));

        assertThrows(
                AuthorizationException.class,
                () -> appointmentService.getAppointmentDetails(42L, "UHID-ATTACKER")
        );

        verify(qrTokenRepository, never()).findByAppointmentId(42L);
        verify(patientClinicalClient, never()).getMedicalRecordForAppointment(42L);
    }

    @Test
    void bookingCreatesOneHourQrWindowAroundAppointment() {
        String patientUhid = "UHID-1";
        LocalDateTime appointmentTime = LocalDateTime.of(2026, 8, 10, 13, 0);

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setUhid(patientUhid);

        Hospital hospital = new Hospital();
        hospital.setId("H1");

        Doctor doctor = new Doctor();
        doctor.setId(7L);
        doctor.setHospital(hospital);

        AppointmentRequest request = new AppointmentRequest();
        request.setHospitalId("H1");
        request.setDoctorId(7L);
        request.setAppointmentTime(appointmentTime.toString());

        when(patientRepository.findByUhid(patientUhid)).thenReturn(Optional.of(patient));
        when(hospitalRepository.findById("H1")).thenReturn(Optional.of(hospital));
        when(doctorRepository.findById(7L)).thenReturn(Optional.of(doctor));
        when(slotLockService.acquireSlotLock(7L, appointmentTime)).thenReturn(true);
        when(appointmentRepository.existsByDoctorAndAppointmentTime(doctor, appointmentTime)).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        appointmentService.bookAppointment(request, patientUhid);

        ArgumentCaptor<QRToken> qrCaptor = ArgumentCaptor.forClass(QRToken.class);
        verify(qrTokenRepository).save(qrCaptor.capture());

        assertEquals(appointmentTime.minusHours(1), qrCaptor.getValue().getValidFrom());
        assertEquals(appointmentTime.plusHours(1), qrCaptor.getValue().getValidTo());
    }
}
