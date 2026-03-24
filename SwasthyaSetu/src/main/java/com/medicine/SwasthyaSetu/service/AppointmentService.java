package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.Entity.*;
import com.medicine.SwasthyaSetu.dto.AppointmentRequest;
import com.medicine.SwasthyaSetu.dto.AppointmentResponse;
import com.medicine.SwasthyaSetu.repository.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AppointmentService {
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final AppointmentRepository appointmentRepository;
    private final QrTokenRepository qrTokenRepository;
    private final DoctorRepository doctorRepository;

    private static final Logger log = LoggerFactory.getLogger(HospitalServices.class);

    public AppointmentService(PatientRepository patientRepository,
                              HospitalRepository hospitalRepository,
                              AppointmentRepository appointmentRepository,
                              QrTokenRepository qrTokenRepository,
                              DoctorRepository doctorRepository
    ){
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.appointmentRepository = appointmentRepository;
        this.qrTokenRepository = qrTokenRepository;
        this.doctorRepository = doctorRepository;
    }

    public AppointmentResponse bookAppointment(AppointmentRequest request){

        // Get Patient
        Patient patient = patientRepository.findByUhid(request.getUhid()).orElseThrow(
                () -> new RuntimeException("Patient not found"));

        // Get hospital
        Hospital hospital = hospitalRepository.findById(request.getHospitalId()).orElseThrow(
                () -> new RuntimeException("Hospital not found")
        );

        // Get doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId()).orElseThrow(
                () -> new RuntimeException("Hospital not found")
        );

        // Create appointment
        LocalDateTime time = LocalDateTime.parse(request.getAppointmentTime());

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setHospital(hospital);
        appointment.setDoctor(doctor);
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
