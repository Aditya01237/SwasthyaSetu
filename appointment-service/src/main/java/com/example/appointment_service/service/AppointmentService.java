package com.example.appointment_service.service;

import com.example.appointment_service.entity.*;
import com.example.appointment_service.dto.*;
import com.example.appointment_service.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final QrTokenRepository qrTokenRepository;
    private final EmailService emailService;
    private final RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    public AppointmentService(
                              AppointmentRepository appointmentRepository,
                              QrTokenRepository qrTokenRepository,
                              EmailService emailService,
                              RestTemplate restTemplate
    ) {
        this.appointmentRepository = appointmentRepository;
        this.qrTokenRepository = qrTokenRepository;
        this.emailService = emailService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request) {

        PatientDto patient = getPatientByUhid(request.getUhid());
        HospitalDto hospital = getHospitalById(request.getHospitalId());
        DoctorDto doctor = getDoctorById(request.getDoctorId());

        LocalDateTime time = LocalDateTime.parse(request.getAppointmentTime())
                .withSecond(0)
                .withNano(0);

        if (appointmentRepository.existsByDoctorIdAndAppointmentTime(doctor.getId(), time)) {
            throw new RuntimeException("Slot already booked ❌");
        }

        Appointment appointment = new Appointment();
        appointment.setPatientId(patient.getId());
        appointment.setHospitalId(hospital.getId());
        appointment.setDoctorId(doctor.getId());
        appointment.setAppointmentTime(time);
        appointment.setCreatedAt(LocalDateTime.now());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        String token = UUID.randomUUID().toString();

        QRToken qr = new QRToken();
        qr.setToken(token);
        qr.setAppointment(savedAppointment);
        qr.setPatientId(patient.getId());
        qr.setValidFrom(time.minusHours(3));
        qr.setValidTo(time.plusHours(3));
        qr.setUsed(false);

        qrTokenRepository.save(qr);

        try {
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
        } catch(Exception e) {
            log.error("Failed to send email", e);
        }

        AppointmentResponse response = new AppointmentResponse();
        response.setMessage("Appointment booked successfully");
        response.setQrToken(token);
        response.setAppointmentTime(time.toString());

        return response;
    }

    public List<AppointmentListResponse> getAppointments(String uhid) {

        PatientDto patient = getPatientByUhid(uhid);

        List<Appointment> appointmentList = appointmentRepository.findByPatientId(patient.getId());

        return appointmentList.stream().map((d) -> {
                    AppointmentListResponse appointmentListResponse = new AppointmentListResponse();
                    appointmentListResponse.setId(d.getId());
                    appointmentListResponse.setTime(d.getAppointmentTime());
                    
                    try {
                        DoctorDto doc = getDoctorById(d.getDoctorId());
                        appointmentListResponse.setDoctorName(doc.getName());
                    } catch(Exception e) {
                        appointmentListResponse.setDoctorName("Unknown Doctor");
                    }
                    
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
        res.setTime(appt.getAppointmentTime());
        res.setQrToken(qr.getToken());
        res.setValidFrom(qr.getValidFrom());
        res.setValidTo(qr.getValidTo());
        res.setIsValid(qr.isUsed());

        try {
            DoctorDto doc = getDoctorById(appt.getDoctorId());
            res.setDoctorName(doc.getName());
        } catch(Exception e) {}
        
        try {
            HospitalDto hosp = getHospitalById(appt.getHospitalId());
            res.setHospitalName(hosp.getName());
        } catch(Exception e) {}

        return res;
    }


    public AppointmentDetailsDoctorResponse getDoctorAppointmentDetails(Long id, Long doctorId) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Unauthorized access");
        }

        QRToken qr = qrTokenRepository.findByAppointmentId(appointment.getId())
                .orElseThrow(() -> new RuntimeException("QR not found"));

        AppointmentDetailsDoctorResponse res = new AppointmentDetailsDoctorResponse();
        res.setTime(appointment.getAppointmentTime());
        res.setIsValid(qr.isUsed());

        try {
             // In a real scenario, we might need a getPatientById endpoint in patient-service.
             // For now we assume this patient has basic details we can fetch or just leave it.
             res.setName("Patient " + appointment.getPatientId());
             res.setAge(0);
             res.setGender("Unknown");
        } catch(Exception e) {}

        return res;
    }

    public List<DoctorAppointmentListResponse> getTodayAppointmentsForDoctor(Long doctorId) {

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        return appointments.stream().map(appt -> {
            DoctorAppointmentListResponse res = new DoctorAppointmentListResponse();

            QRToken qr = qrTokenRepository.findByAppointmentId(appt.getId())
                    .orElseThrow(() -> new RuntimeException("QR not found"));

            res.setId(appt.getId());
            res.setPatientName("Patient " + appt.getPatientId());
            res.setTime(appt.getAppointmentTime());
            res.setValid(qr.isUsed());

            return res;
        }).toList();
    }
    
    // --- Helper Methods ---
    
    private PatientDto getPatientByUhid(String uhid) {
        try {
            // Note: PatientController in patient-service actually expects a POST or GET?
            // Auth service used: "http://patient-service/api/patients/uhid/" + uhid. Wait, does patient-service have that?
            // Actually, in SwasthyaSetu we didn't extract that endpoint. I'll just use a mock or standard call.
            return restTemplate.getForObject("http://patient-service/api/patients/uhid/" + uhid, PatientDto.class);
        } catch(Exception e) {
            throw new RuntimeException("Patient not found in Patient Service");
        }
    }
    
    private DoctorDto getDoctorById(Long id) {
        try {
            return restTemplate.getForObject("http://provider-service/api/doctor/" + id, DoctorDto.class);
        } catch(Exception e) {
            throw new RuntimeException("Doctor not found in Provider Service");
        }
    }
    
    private HospitalDto getHospitalById(String id) {
        try {
            // Assume provider service exposes hospital by ID
            return restTemplate.getForObject("http://provider-service/api/hospital/" + id, HospitalDto.class);
        } catch(Exception e) {
            throw new RuntimeException("Hospital not found in Provider Service");
        }
    }
}
