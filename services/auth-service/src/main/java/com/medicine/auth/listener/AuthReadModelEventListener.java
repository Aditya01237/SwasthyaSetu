package com.medicine.auth.listener;

import com.medicine.auth.entity.Doctor;
import com.medicine.auth.entity.Hospital;
import com.medicine.auth.entity.Patient;
import com.medicine.auth.event.DoctorRegisteredEvent;
import com.medicine.auth.event.HospitalUpsertedEvent;
import com.medicine.auth.event.PatientRegisteredEvent;
import com.medicine.auth.repository.DoctorRepository;
import com.medicine.auth.repository.HospitalRepository;
import com.medicine.auth.repository.PatientRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AuthReadModelEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthReadModelEventListener.class);

    private final ObjectMapper objectMapper;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public AuthReadModelEventListener(ObjectMapper objectMapper,
                                      PatientRepository patientRepository,
                                      HospitalRepository hospitalRepository,
                                      DoctorRepository doctorRepository,
                                      EntityManager entityManager,
                                      TransactionTemplate transactionTemplate) {
        this.objectMapper = objectMapper;
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
        this.entityManager = entityManager;
        this.transactionTemplate = transactionTemplate;
    }

    @RabbitListener(queues = "${app.events.queues.auth-patient-registered}")
    public void handlePatientRegistered(String payload) {
        transactionTemplate.execute(status -> {
            try {
                PatientRegisteredEvent event = objectMapper.readValue(payload, PatientRegisteredEvent.class);
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
                log.info("Synced patient {} (uhid={}) into auth read model", event.name(), event.uhid());
            } catch (Exception ex) {
                log.error("Failed to sync patient.registered into auth read model", ex);
                status.setRollbackOnly();
            }
            return null;
        });
    }

    @RabbitListener(queues = "${app.events.queues.auth-hospital-upserted}")
    public void handleHospitalUpserted(String payload) {
        transactionTemplate.execute(status -> {
            try {
                HospitalUpsertedEvent event = objectMapper.readValue(payload, HospitalUpsertedEvent.class);
                Hospital hospital = hospitalRepository.findById(event.id()).orElseGet(Hospital::new);
                applyHospital(hospital, event);
                hospitalRepository.save(hospital);
                log.info("Synced hospital {} into auth read model", event.id());
            } catch (Exception ex) {
                log.error("Failed to sync hospital.upserted into auth read model", ex);
                status.setRollbackOnly();
            }
            return null;
        });
    }

    @RabbitListener(queues = "${app.events.queues.auth-doctor-registered}")
    public void handleDoctorRegistered(String payload) {
        transactionTemplate.execute(status -> {
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
                if (doctor.getId() != null) {
                    entityManager.merge(doctor);
                } else {
                    doctorRepository.save(doctor);
                }
                log.info("Synced doctor {} (email={}) into auth read model", event.name(), event.email());
            } catch (Exception ex) {
                log.error("Failed to sync doctor.registered into auth read model", ex);
                status.setRollbackOnly();
            }
            return null;
        });
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
