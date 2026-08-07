package com.medicine.auth.listener;

import com.medicine.auth.entity.Doctor;
import com.medicine.auth.entity.DoctorInvitation;
import com.medicine.auth.entity.Hospital;
import com.medicine.auth.entity.Patient;
import com.medicine.auth.event.DoctorRegisteredEvent;
import com.medicine.auth.event.HospitalUpsertedEvent;
import com.medicine.auth.event.PatientRegisteredEvent;
import com.medicine.auth.repository.DoctorInvitationRepository;
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
import java.util.Optional;

@Component
public class AuthReadModelEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthReadModelEventListener.class);

    private final ObjectMapper objectMapper;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorInvitationRepository doctorInvitationRepository;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public AuthReadModelEventListener(ObjectMapper objectMapper,
                                      PatientRepository patientRepository,
                                      HospitalRepository hospitalRepository,
                                      DoctorRepository doctorRepository,
                                      DoctorInvitationRepository doctorInvitationRepository,
                                      EntityManager entityManager,
                                      TransactionTemplate transactionTemplate) {
        this.objectMapper = objectMapper;
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
        this.doctorInvitationRepository = doctorInvitationRepository;
        this.entityManager = entityManager;
        this.transactionTemplate = transactionTemplate;
    }

    @RabbitListener(queues = "${app.events.queues.auth-patient-registered}")
    public void handlePatientRegistered(String payload) {
        transactionTemplate.execute(status -> {
            try {
                PatientRegisteredEvent event = objectMapper.readValue(payload, PatientRegisteredEvent.class);
                Patient patient = findPatient(event);
                if (event.id() != null) patient.setId(event.id());
                patient.setUhid(event.uhid());
                patient.setName(event.name());
                patient.setEmail(event.email());
                patient.setPhone(event.phone());
                patient.setAge(event.age() != null ? event.age() : 0);
                patient.setGender(event.gender());
                patient.setCreatedAt(parseDateTime(event.createdAt()));
                if (patient.getId() != null) entityManager.merge(patient);
                else patientRepository.save(patient);
                log.info("Synced patient {} (uhid={}) into auth read model", event.name(), event.uhid());
                return null;
            } catch (Exception ex) {
                status.setRollbackOnly();
                log.error("Failed to sync patient.registered; message will be retried", ex);
                throw new RuntimeException("patient.registered auth read-model sync failed", ex);
            }
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
                return null;
            } catch (Exception ex) {
                status.setRollbackOnly();
                log.error("Failed to sync hospital.upserted; message will be retried", ex);
                throw new RuntimeException("hospital.upserted auth read-model sync failed", ex);
            }
        });
    }

    @RabbitListener(queues = "${app.events.queues.auth-doctor-registered}")
    public void handleDoctorRegistered(String payload) {
        transactionTemplate.execute(status -> {
            try {
                DoctorRegisteredEvent event = objectMapper.readValue(payload, DoctorRegisteredEvent.class);
                validateDoctorProfileEvent(event);

                Optional<Doctor> existing = findExistingDoctor(event);
                if (existing.isEmpty()) {
                    upsertInvitation(event);
                    log.info("Created/updated doctor invitation for profile id={} email={}", event.id(), event.email());
                    return null;
                }

                Doctor doctor = existing.get();
                doctor.setProfileId(event.id());
                doctor.setName(event.name());
                doctor.setSpecialization(event.specialization());
                doctor.setExperience(event.experience());
                doctor.setFee(event.fee());
                doctor.setEmail(event.email().trim().toLowerCase());
                hospitalRepository.findById(event.hospitalId()).ifPresent(doctor::setHospital);
                doctorRepository.save(doctor);
                log.info("Synced doctor profile {} (profileId={}, email={}) into auth account",
                        event.name(), event.id(), event.email());
                return null;
            } catch (Exception ex) {
                status.setRollbackOnly();
                log.error("Failed to sync doctor.registered; message will be retried", ex);
                throw new RuntimeException("doctor.registered auth read-model sync failed", ex);
            }
        });
    }

    private void validateDoctorProfileEvent(DoctorRegisteredEvent event) {
        if (event.id() == null || event.email() == null || event.email().isBlank()
                || event.hospitalId() == null || event.hospitalId().isBlank()) {
            throw new IllegalArgumentException("doctor.registered must include id, email and hospitalId");
        }
    }

    private void upsertInvitation(DoctorRegisteredEvent event) {
        DoctorInvitation invitation = doctorInvitationRepository.findById(event.id())
                .orElseGet(() -> doctorInvitationRepository.findByEmail(event.email().trim().toLowerCase())
                        .orElseGet(DoctorInvitation::new));
        invitation.setDoctorId(event.id());
        invitation.setEmail(event.email().trim().toLowerCase());
        invitation.setHospitalId(event.hospitalId());
        invitation.setName(event.name());
        invitation.setSpecialization(event.specialization());
        invitation.setExperience(event.experience());
        invitation.setFee(event.fee());
        invitation.setStatus("PENDING");
        invitation.setAcceptedAt(null);
        doctorInvitationRepository.save(invitation);
    }

    private Patient findPatient(PatientRegisteredEvent event) {
        if (event.id() != null) {
            return patientRepository.findById(event.id())
                    .orElseGet(() -> patientRepository.findByUhid(event.uhid()).orElseGet(Patient::new));
        }
        return patientRepository.findByUhid(event.uhid()).orElseGet(Patient::new);
    }

    private Optional<Doctor> findExistingDoctor(DoctorRegisteredEvent event) {
        if (event.id() != null) {
            Optional<Doctor> byProfileId = doctorRepository.findByProfileId(event.id());
            if (byProfileId.isPresent()) return byProfileId;
        }
        return doctorRepository.findByEmail(event.email().trim().toLowerCase());
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
