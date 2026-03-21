package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.Entity.Appointment;
import com.medicine.SwasthyaSetu.Entity.Hospital;
import com.medicine.SwasthyaSetu.Entity.Patient;
import com.medicine.SwasthyaSetu.Entity.QRToken;
import com.medicine.SwasthyaSetu.dto.AppointmentRequest;
import com.medicine.SwasthyaSetu.dto.AppointmentResponse;
import com.medicine.SwasthyaSetu.repository.AppointmentRepository;
import com.medicine.SwasthyaSetu.repository.HospitalRepository;
import com.medicine.SwasthyaSetu.repository.PatientRepository;
import com.medicine.SwasthyaSetu.repository.QrTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AppointmentService {
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final AppointmentRepository appointmentRepository;
    private final QrTokenRepository qrTokenRepository;

    public AppointmentService(PatientRepository patientRepository,
                              HospitalRepository hospitalRepository,
                              AppointmentRepository appointmentRepository,
                              QrTokenRepository qrTokenRepository){
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.appointmentRepository = appointmentRepository;
        this.qrTokenRepository = qrTokenRepository;
    }

    public AppointmentResponse bookAppointment(AppointmentRequest request){

        // Get Patient
        Patient patient = patientRepository.findByUhid(request.getUhid()).orElseThrow(
                () -> new RuntimeException("Patient not found"));

        // Get hospital
        Hospital hospital = hospitalRepository.findById(request.getHospitalId()).orElseThrow(
                () -> new RuntimeException("Hospital not found")
        );

        // Create appointment
        LocalDateTime time = LocalDateTime.parse(request.getAppointmentTime());

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setHospital(hospital);
        appointment.setAppointmentTime(time);
        appointment.setCreatedAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Generate QR Token
        String token = UUID.randomUUID().toString();

        QRToken qr = new QRToken();
        qr.setToken(token);
        qr.setAppointment(savedAppointment);
        qr.setPatient(patient);

        qr.setValidFrom(time.minusHours(3));
        qr.setValidTo(time.plusHours(3));
        qr.setUsed(false);

        qrTokenRepository.save(qr);

        // Response
        AppointmentResponse response = new AppointmentResponse();
        response.setMessage("Appointment booked successfully");
        response.setQrToken(token);

        return response;
    }

}
