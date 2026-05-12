package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.Entity.*;
import com.medicine.SwasthyaSetu.dto.*;
import com.medicine.SwasthyaSetu.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final AppointmentRepository appointmentRepository;
    private final QrTokenRepository qrTokenRepository;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    public AppointmentService(PatientRepository patientRepository,
                              HospitalRepository hospitalRepository,
                              AppointmentRepository appointmentRepository,
                              QrTokenRepository qrTokenRepository,
                              DoctorRepository doctorRepository,
                              EmailService emailService
    ) {
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.appointmentRepository = appointmentRepository;
        this.qrTokenRepository = qrTokenRepository;
        this.doctorRepository = doctorRepository;
        this.emailService = emailService;
    }

    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request) {

        Patient patient = patientRepository.findByUhid(request.getUhid())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // ✅ Validate doctor belongs to hospital
        if (!doctor.getHospital().getId().equals(hospital.getId())) {
            throw new RuntimeException("Doctor does not belong to this hospital");
        }

        LocalDateTime time = LocalDateTime.parse(request.getAppointmentTime())
                .withSecond(0)
                .withNano(0);

        // ✅ Slot check
        if (appointmentRepository.existsByDoctorAndAppointmentTime(doctor, time)) {
            throw new RuntimeException("Slot already booked ❌");
        }

        time = LocalDateTime.parse(request.getAppointmentTime());

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setHospital(hospital);
        appointment.setDoctor(doctor);
        appointment.setAppointmentTime(time);
        appointment.setCreatedAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // QR
        String token = UUID.randomUUID().toString();

        QRToken qr = new QRToken();
        qr.setToken(token);
        qr.setAppointment(savedAppointment);
        qr.setPatient(patient);
        qr.setValidFrom(time.minusHours(3));
        qr.setValidTo(time.plusHours(3));
        qr.setUsed(false);

        qrTokenRepository.save(qr);

        // After qrTokenRepository.save(qr);

        emailService.sendAppointmentConfirmationEmail(
                patient.getEmail(),
                patient.getName(),
                doctor.getName(),
                doctor.getSpecialization(),
                hospital.getName(),
                hospital.getAddress(),
                time.toString(),
                token
        );

        AppointmentResponse response = new AppointmentResponse();
        response.setMessage("Appointment booked successfully");
        response.setQrToken(token);
        response.setAppointmentTime(time.toString());

        return response;
    }

    public List<AppointmentListResponse> getAppointments(String uhid) {

        Patient patient = patientRepository.findByUhid(uhid)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<Appointment> appointmentList = appointmentRepository.findByPatientId(patient.getId());

        return appointmentList.stream().map((d) -> {
                    AppointmentListResponse appointmentListResponse = new AppointmentListResponse();
                    appointmentListResponse.setId(d.getId());
                    appointmentListResponse.setTime(d.getAppointmentTime());
                    appointmentListResponse.setDoctorName(d.getDoctor().getName());
                    return appointmentListResponse;
                }
        ).toList();
    }

    public AppointmentDetailsResponse getAppointmentDetails(Long id) {

        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        QRToken qr = qrTokenRepository.findByAppointmentId(id)
                .orElseThrow(() -> new RuntimeException("QR not found"));

        AppointmentDetailsResponse res = new AppointmentDetailsResponse();
        res.setId(appt.getId());
        res.setDoctorName(appt.getDoctor().getName());
        res.setHospitalName(appt.getHospital().getName());
        res.setTime(appt.getAppointmentTime());
        res.setQrToken(qr.getToken());
        res.setValidFrom(qr.getValidFrom());
        res.setValidTo(qr.getValidTo());
        res.setIsValid(qr.isUsed()); // ✅ tells patient side QR was scanned by doctor

        return res;
    }


    public AppointmentDetailsDoctorResponse getDoctorAppointmentDetails(Long id, Long doctorId) {

        // 🔍 1. Fetch appointment
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // 2. Fetch patient
        Patient patient = appointment.getPatient();


        // 🚨 3. SECURITY CHECK
        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Unauthorized access");
        }

        // 🔎 4. Fetch QR Token
        QRToken qr = qrTokenRepository.findByAppointmentId(appointment.getId())
                .orElseThrow(() -> new RuntimeException("QR not found"));

        // 🧾 5. Map response
        AppointmentDetailsDoctorResponse res = new AppointmentDetailsDoctorResponse();

        res.setName(patient.getName());
        res.setAge(patient.getAge());
        res.setGender(patient.getGender());
        res.setTime(appointment.getAppointmentTime());
        res.setIsValid(qr.isUsed());

        return res;
    }

    public List<DoctorAppointmentListResponse> getTodayAppointmentsForDoctor(Long doctorId) {

        System.out.println("DOCTOR ID: " + doctorId);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        return appointments.stream().map(appt -> {
            DoctorAppointmentListResponse res = new DoctorAppointmentListResponse();

            QRToken qr = qrTokenRepository.findByAppointmentId(appt.getId())
                    .orElseThrow(() -> new RuntimeException("QR not found"));

            res.setId(appt.getId());
            res.setPatientName(appt.getPatient().getName());
            res.setTime(appt.getAppointmentTime());
            res.setValid(qr.isUsed());

            return res;
        }).toList();
    }

}
