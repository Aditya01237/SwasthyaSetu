package com.medicine.patient.listener;

import com.medicine.patient.entity.Appointment;
import com.medicine.patient.entity.Doctor;
import com.medicine.patient.entity.Hospital;
import com.medicine.patient.entity.Patient;
import com.medicine.patient.event.AppointmentBookedEvent;
import com.medicine.patient.event.DoctorRegisteredEvent;
import com.medicine.patient.event.HospitalUpsertedEvent;
import com.medicine.patient.repository.AppointmentRepository;
import com.medicine.patient.repository.DoctorRepository;
import com.medicine.patient.repository.HospitalRepository;
import com.medicine.patient.repository.PatientRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class PatientReadModelEventListener {

    private static final Logger log = LoggerFactory.getLogger(PatientReadModelEventListener.class);

    private final ObjectMapper objectMapper;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;

    public PatientReadModelEventListener(ObjectMapper objectMapper,
                                         AppointmentRepository appointmentRepository,
                                         PatientRepository patientRepository,
                                         HospitalRepository hospitalRepository,
                                         DoctorRepository doctorRepository) {
        this.objectMapper = objectMapper;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    @RabbitListener(queues = "${app.events.queues.patient-appointment-booked}")
    public void handleAppointmentBooked(String payload) {
        try {
            AppointmentBookedEvent event = objectMapper.readValue(payload, AppointmentBookedEvent.class);
            Optional<Patient> patient = findPatient(event);
            Optional<Hospital> hospital = event.hospitalId() == null ? Optional.empty() : hospitalRepository.findById(event.hospitalId());
            Optional<Doctor> doctor = event.doctorId() == null ? Optional.empty() : doctorRepository.findById(event.doctorId());

            if (patient.isEmpty() || hospital.isEmpty() || doctor.isEmpty()) {
                log.warn(
                        "Skipping appointment.booked sync because read-model dependency is missing: appointment={}, patient={}, hospital={}, doctor={}",
                        event.appointmentId(),
                        patient.isPresent(),
                        hospital.isPresent(),
                        doctor.isPresent()
                );
                return;
            }

            Appointment appointment = event.appointmentId() == null
                    ? new Appointment()
                    : appointmentRepository.findById(event.appointmentId()).orElseGet(Appointment::new);
            if (event.appointmentId() != null) {
                appointment.setId(event.appointmentId());
            }
            appointment.setPatient(patient.get());
            appointment.setHospital(hospital.get());
            appointment.setDoctor(doctor.get());
            appointment.setAppointmentTime(parseDateTime(event.appointmentTime()));
            appointment.setCreatedAt(parseDateTime(event.createdAt()));
            appointmentRepository.save(appointment);
        } catch (Exception ex) {
            log.error("Failed to sync appointment.booked into patient read model", ex);
        }
    }

    @Transactional
    @RabbitListener(queues = "${app.events.queues.patient-hospital-upserted}")
    public void handleHospitalUpserted(String payload) {
        try {
            HospitalUpsertedEvent event = objectMapper.readValue(payload, HospitalUpsertedEvent.class);
            Hospital hospital = hospitalRepository.findById(event.id()).orElseGet(Hospital::new);
            applyHospital(hospital, event);
            hospitalRepository.save(hospital);
        } catch (Exception ex) {
            log.error("Failed to sync hospital.upserted into patient read model", ex);
        }
    }

    @Transactional
    @RabbitListener(queues = "${app.events.queues.patient-doctor-registered}")
    public void handleDoctorRegistered(String payload) {
        try {
            DoctorRegisteredEvent event = objectMapper.readValue(payload, DoctorRegisteredEvent.class);
            Doctor doctor = findDoctor(event);
            if (event.id() != null) {
                doctor.setId(event.id());
            }
            doctor.setName(event.name());
            doctor.setSpecialization(event.specialization());
            doctor.setExperience(event.experience());
            doctor.setFee(event.fee());
            doctor.setEmail(event.email());
            doctor.setPassword(event.password());
            if (event.hospitalId() != null) {
                hospitalRepository.findById(event.hospitalId()).ifPresent(doctor::setHospital);
            }
            doctorRepository.save(doctor);
        } catch (Exception ex) {
            log.error("Failed to sync doctor.registered into patient read model", ex);
        }
    }

    private Optional<Patient> findPatient(AppointmentBookedEvent event) {
        if (event.patientId() != null) {
            Optional<Patient> byId = patientRepository.findById(event.patientId());
            if (byId.isPresent()) {
                return byId;
            }
        }
        if (event.patientUhid() != null) {
            return patientRepository.findByUhid(event.patientUhid());
        }
        return Optional.empty();
    }

    private Doctor findDoctor(DoctorRegisteredEvent event) {
        if (event.id() != null) {
            return doctorRepository.findById(event.id()).orElseGet(() -> doctorRepository.findByEmail(event.email()).orElseGet(Doctor::new));
        }
        return doctorRepository.findByEmail(event.email()).orElseGet(Doctor::new);
    }

    private void applyHospital(Hospital hospital, HospitalUpsertedEvent event) {
        hospital.setId(event.id());
        hospital.setName(event.name());
        hospital.setCity(event.city());
        hospital.setAddress(event.address());
        hospital.setPhone(event.phone());
        hospital.setEmail(event.email());
        hospital.setImageUrls(event.imageUrls() != null ? event.imageUrls() : List.of());
        hospital.setRating(event.rating());
        hospital.setTotalReviews(event.totalReviews());
        hospital.setServices(event.services() != null ? event.services() : List.of());
        hospital.setSpecializations(event.specializations() != null ? event.specializations() : List.of());
        hospital.setIsOpen24x7(event.isOpen24x7());
    }

    private LocalDateTime parseDateTime(String value) {
        return value == null || value.isBlank() ? LocalDateTime.now() : LocalDateTime.parse(value);
    }
}
