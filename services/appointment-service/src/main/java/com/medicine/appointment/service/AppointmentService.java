package com.medicine.appointment.service;

import com.medicine.appointment.dto.AppointmentDetailsDoctorResponse;
import com.medicine.appointment.dto.AppointmentDetailsResponse;
import com.medicine.appointment.dto.AppointmentListResponse;
import com.medicine.appointment.dto.AppointmentReadModelResponse;
import com.medicine.appointment.dto.AppointmentRequest;
import com.medicine.appointment.dto.AppointmentResponse;
import com.medicine.appointment.dto.DoctorAppointmentListResponse;
import com.medicine.appointment.entity.Appointment;
import com.medicine.appointment.entity.Doctor;
import com.medicine.appointment.entity.Hospital;
import com.medicine.appointment.entity.Patient;
import com.medicine.appointment.entity.QRToken;
import com.medicine.appointment.repository.AppointmentRepository;
import com.medicine.appointment.repository.DoctorRepository;
import com.medicine.appointment.repository.HospitalRepository;
import com.medicine.appointment.repository.PatientRepository;
import com.medicine.appointment.repository.QrTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final DateTimeFormatter SLOT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final AppointmentRepository appointmentRepository;
    private final QrTokenRepository qrTokenRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentEventPublisher appointmentEventPublisher;
    private final SlotLockService slotLockService;
    private final PatientClinicalClient patientClinicalClient;

    public AppointmentService(PatientRepository patientRepository,
                              HospitalRepository hospitalRepository,
                              AppointmentRepository appointmentRepository,
                              QrTokenRepository qrTokenRepository,
                              DoctorRepository doctorRepository,
                              AppointmentEventPublisher appointmentEventPublisher,
                              SlotLockService slotLockService,
                              PatientClinicalClient patientClinicalClient) {
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.appointmentRepository = appointmentRepository;
        this.qrTokenRepository = qrTokenRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentEventPublisher = appointmentEventPublisher;
        this.slotLockService = slotLockService;
        this.patientClinicalClient = patientClinicalClient;
    }

    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request) {
        Patient patient = patientRepository.findByUhid(request.getUhid())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (doctor.getHospital() == null || !doctor.getHospital().getId().equals(hospital.getId())) {
            throw new RuntimeException("Doctor does not belong to this hospital");
        }

        LocalDateTime time = LocalDateTime.parse(request.getAppointmentTime()).withSecond(0).withNano(0);

        if (!slotLockService.acquireSlotLock(doctor.getId(), time)) {
            throw new RuntimeException("Slot already being booked. Please try another slot.");
        }

        if (appointmentRepository.existsByDoctorAndAppointmentTime(doctor, time)) {
            throw new RuntimeException("Slot already booked");
        }

        try {
            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setHospital(hospital);
            appointment.setDoctor(doctor);
            appointment.setAppointmentTime(time);
            appointment.setCreatedAt(LocalDateTime.now());

            Appointment savedAppointment = appointmentRepository.save(appointment);
            String token = UUID.randomUUID().toString();

            QRToken qr = new QRToken();
            qr.setToken(token);
            qr.setAppointment(savedAppointment);
            qr.setPatient(patient);
            qr.setValidFrom(LocalDateTime.now());  // valid from booking time (avoids UTC/IST mismatch)
            qr.setValidTo(time.plusHours(3));        // valid until 3h after appointment
            qr.setUsed(false);
            qrTokenRepository.save(qr);

            appointmentEventPublisher.publishAppointmentBooked(savedAppointment, token);

            AppointmentResponse response = new AppointmentResponse();
            response.setMessage("Appointment booked successfully");
            response.setQrToken(token);
            response.setAppointmentTime(time.toString());
            return response;
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Slot already booked", ex);
        }
    }

    public List<AppointmentListResponse> getAppointments(String uhid) {
        Patient patient = patientRepository.findByUhid(uhid)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        return appointmentRepository.findByPatientId(patient.getId())
                .stream()
                .map(appointment -> {
                    AppointmentListResponse response = new AppointmentListResponse();
                    response.setId(appointment.getId());
                    response.setTime(appointment.getAppointmentTime());
                    response.setDoctorName(appointment.getDoctor().getName());
                    return response;
                })
                .toList();
    }

    public AppointmentDetailsResponse getAppointmentDetails(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        QRToken qr = qrTokenRepository.findByAppointmentId(id)
                .orElseThrow(() -> new RuntimeException("QR not found"));

        AppointmentDetailsResponse response = new AppointmentDetailsResponse();
        response.setId(appointment.getId());
        response.setDoctorName(appointment.getDoctor().getName());
        response.setHospitalName(appointment.getHospital().getName());
        response.setTime(appointment.getAppointmentTime());
        response.setQrToken(qr.getToken());
        response.setValidFrom(qr.getValidFrom());
        response.setValidTo(qr.getValidTo());
        response.setIsValid(qr.isUsed());

        patientClinicalClient.getMedicalRecordForAppointment(id)
                .ifPresent(response::setMedicalRecord);

        return response;
    }

    public AppointmentDetailsDoctorResponse getDoctorAppointmentDetails(Long id, Long doctorId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Unauthorized access");
        }

        QRToken qr = qrTokenRepository.findByAppointmentId(appointment.getId())
                .orElseThrow(() -> new RuntimeException("QR not found"));

        Patient patient = appointment.getPatient();
        AppointmentDetailsDoctorResponse response = new AppointmentDetailsDoctorResponse();
        response.setName(patient.getName());
        response.setAge(patient.getAge());
        response.setGender(patient.getGender());
        response.setTime(appointment.getAppointmentTime());
        response.setIsValid(qr.isUsed());
        return response;
    }

    public List<DoctorAppointmentListResponse> getTodayAppointmentsForDoctor(Long doctorId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        return appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay)
                .stream()
                .map(appointment -> {
                    QRToken qr = qrTokenRepository.findByAppointmentId(appointment.getId())
                            .orElseThrow(() -> new RuntimeException("QR not found"));

                    DoctorAppointmentListResponse response = new DoctorAppointmentListResponse();
                    response.setId(appointment.getId());
                    response.setPatientName(appointment.getPatient().getName());
                    response.setTime(appointment.getAppointmentTime());
                    response.setValid(qr.isUsed());
                    return response;
                })
                .toList();
    }

    public List<String> getBookedSlotsForDoctor(Long doctorId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay)
                .stream()
                .map(appointment -> appointment.getAppointmentTime().toLocalTime().format(SLOT_TIME_FORMATTER))
                .toList();
    }

    @Transactional
    public AppointmentReadModelResponse getReadModelSnapshot(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        Patient patient = appointment.getPatient();
        return new AppointmentReadModelResponse(
                appointment.getId(),
                appointment.getAppointmentTime().toString(),
                appointment.getCreatedAt() != null ? appointment.getCreatedAt().toString() : null,
                patient.getId(),
                patient.getUhid(),
                appointment.getDoctor().getId(),
                appointment.getHospital().getId()
        );
    }

}
