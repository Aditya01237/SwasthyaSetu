package com.medicine.appointment.listener;

import com.medicine.appointment.entity.Doctor;
import com.medicine.appointment.entity.Hospital;
import com.medicine.appointment.entity.Patient;
import com.medicine.appointment.event.DoctorRegisteredEvent;
import com.medicine.appointment.event.HospitalUpsertedEvent;
import com.medicine.appointment.event.PatientRegisteredEvent;
import com.medicine.appointment.repository.DoctorRepository;
import com.medicine.appointment.repository.HospitalRepository;
import com.medicine.appointment.repository.PatientRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AppointmentReadModelEventListener {

    private static final Logger log = LoggerFactory.getLogger(AppointmentReadModelEventListener.class);

    private final ObjectMapper objectMapper;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final EntityManager entityManager;

    public AppointmentReadModelEventListener(ObjectMapper objectMapper,
                                             PatientRepository patientRepository,
                                             HospitalRepository hospitalRepository,
                                             DoctorRepository doctorRepository,
                                             EntityManager entityManager) {
        this.objectMapper = objectMapper;
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
        this.entityManager = entityManager;
    }

    @RabbitListener(queues = "${app.events.queues.appointment-patient-registered}")
    public void handlePatientRegistered(String payload) {
        try {
            PatientRegisteredEvent event = objectMapper.readValue(payload, PatientRegisteredEvent.class);
            syncPatient(event);
        } catch (Exception ex) {
            log.error("Failed to sync patient.registered into appointment read model", ex);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncPatient(PatientRegisteredEvent event) {
        Patient patient = findPatient(event);
        if (event.id() != null) {
            patient.setId(event.id());
        }
        patient.setUhid(event.uhid());
        patient.setName(event.name());
        patient.setEmail(event.email());
        patient.setPhone(event.phone());
        patient.setAge(event.age() != null ? event.age() : 0);
        patient.setGender(event.gender());
        patient.setCreatedAt(parseDateTime(event.createdAt()));
        if (patient.getId() != null) {
            entityManager.merge(patient);
        } else {
            patientRepository.save(patient);
        }
        log.info("Synced patient {} (uhid={}) into appointment read model", event.name(), event.uhid());
    }

    @RabbitListener(queues = "${app.events.queues.appointment-hospital-upserted}")
    public void handleHospitalUpserted(String payload) {
        try {
            HospitalUpsertedEvent event = objectMapper.readValue(payload, HospitalUpsertedEvent.class);
            syncHospital(event);
        } catch (Exception ex) {
            log.error("Failed to sync hospital.upserted into appointment read model", ex);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncHospital(HospitalUpsertedEvent event) {
        Hospital hospital = hospitalRepository.findById(event.id()).orElseGet(Hospital::new);
        applyHospital(hospital, event);
        hospitalRepository.save(hospital);
        log.info("Synced hospital {} into appointment read model", event.id());
    }

    @RabbitListener(queues = "${app.events.queues.appointment-doctor-registered}")
    public void handleDoctorRegistered(String payload) {
        try {
            DoctorRegisteredEvent event = objectMapper.readValue(payload, DoctorRegisteredEvent.class);
            syncDoctor(event);
        } catch (Exception ex) {
            log.error("Failed to sync doctor.registered into appointment read model", ex);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncDoctor(DoctorRegisteredEvent event) {
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
        if (doctor.getId() != null) {
            entityManager.merge(doctor);
        } else {
            doctorRepository.save(doctor);
        }
        log.info("Synced doctor {} (email={}) into appointment read model", event.name(), event.email());
    }

    private Patient findPatient(PatientRegisteredEvent event) {
        if (event.id() != null) {
            return patientRepository.findById(event.id()).orElseGet(() -> patientRepository.findByUhid(event.uhid()).orElseGet(Patient::new));
        }
        return patientRepository.findByUhid(event.uhid()).orElseGet(Patient::new);
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
