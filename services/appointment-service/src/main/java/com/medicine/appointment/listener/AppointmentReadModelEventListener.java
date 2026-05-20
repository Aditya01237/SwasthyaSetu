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
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
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
    private final TransactionTemplate transactionTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public AppointmentReadModelEventListener(ObjectMapper objectMapper,
            PatientRepository patientRepository,
            HospitalRepository hospitalRepository,
            DoctorRepository doctorRepository,
            TransactionTemplate transactionTemplate) {
        this.objectMapper = objectMapper;
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @RabbitListener(queues = "${app.events.queues.appointment-patient-registered}")
    public void handlePatientRegistered(String payload) {
        transactionTemplate.execute(status -> {
            try {
                PatientRegisteredEvent event = objectMapper.readValue(payload, PatientRegisteredEvent.class);

                Patient patient = findManagedPatient(event);

                applyPatient(patient, event);

                savePatientSafely(patient, event);

                log.info("Synced patient {} (uhid={}) into appointment read model",
                        event.name(), event.uhid());

            } catch (Exception ex) {
                log.error("Failed to sync patient.registered into appointment read model", ex);
                status.setRollbackOnly();
            }
            return null;
        });
    }

    @RabbitListener(queues = "${app.events.queues.appointment-hospital-upserted}")
    public void handleHospitalUpserted(String payload) {
        transactionTemplate.execute(status -> {
            try {
                HospitalUpsertedEvent event = objectMapper.readValue(payload, HospitalUpsertedEvent.class);

                Hospital hospital = findManagedHospital(event.id());

                applyHospital(hospital, event);

                saveHospitalSafely(hospital, event);

                log.info("Synced hospital {} into appointment read model", event.id());

            } catch (Exception ex) {
                log.error("Failed to sync hospital.upserted into appointment read model", ex);
                status.setRollbackOnly();
            }
            return null;
        });
    }

    @RabbitListener(queues = "${app.events.queues.appointment-doctor-registered}")
    public void handleDoctorRegistered(String payload) {
        transactionTemplate.execute(status -> {
            try {
                DoctorRegisteredEvent event = objectMapper.readValue(payload, DoctorRegisteredEvent.class);

                if (event.id() == null) {
                    throw new IllegalArgumentException("doctor.registered event id is null");
                }

                Doctor doctor = doctorRepository.findById(event.id())
                        .orElseGet(Doctor::new);

                doctor.setId(event.id());
                doctor.setName(event.name());
                doctor.setSpecialization(event.specialization());
                doctor.setExperience(event.experience());
                doctor.setFee(event.fee());
                doctor.setEmail(event.email());
                doctor.setPassword(event.password());

                if (event.hospitalId() != null && !event.hospitalId().isBlank()) {
                    hospitalRepository.findById(event.hospitalId())
                            .ifPresent(doctor::setHospital);
                }

                doctorRepository.save(doctor);

                log.info("Synced doctor {} with id={} into appointment read model",
                        event.name(), event.id());

            } catch (Exception ex) {
                log.error("Failed to sync doctor.registered into appointment read model", ex);
                status.setRollbackOnly();
            }
            return null;
        });
    }

    private Patient findManagedPatient(PatientRegisteredEvent event) {
        if (event.id() != null) {
            Patient patient = entityManager.find(Patient.class, event.id());
            if (patient != null) {
                return patient;
            }
        }

        if (event.uhid() != null && !event.uhid().isBlank()) {
            return patientRepository.findByUhid(event.uhid()).orElseGet(Patient::new);
        }

        return new Patient();
    }

    private Hospital findManagedHospital(String hospitalId) {
        if (hospitalId != null && !hospitalId.isBlank()) {
            Hospital hospital = entityManager.find(Hospital.class, hospitalId);
            if (hospital != null) {
                return hospital;
            }
        }

        return new Hospital();
    }

    private Doctor findManagedDoctor(DoctorRegisteredEvent event) {
        if (event.id() != null) {
            Doctor doctor = entityManager.find(Doctor.class, event.id());
            if (doctor != null) {
                return doctor;
            }
        }

        if (event.email() != null && !event.email().isBlank()) {
            return doctorRepository.findByEmail(event.email()).orElseGet(Doctor::new);
        }

        return new Doctor();
    }

    private void applyPatient(Patient patient, PatientRegisteredEvent event) {
        if (patient.getId() == null && event.id() != null) {
            patient.setId(event.id());
        }

        patient.setUhid(event.uhid());
        patient.setName(event.name());
        patient.setEmail(event.email());
        patient.setPhone(event.phone());
        patient.setAge(event.age() != null ? event.age() : 0);
        patient.setGender(event.gender());
        patient.setCreatedAt(parseDateTime(event.createdAt()));
    }

    private void applyHospital(Hospital hospital, HospitalUpsertedEvent event) {
        if (hospital.getId() == null && event.id() != null) {
            hospital.setId(event.id());
        }

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

    private void applyDoctor(Doctor doctor, DoctorRegisteredEvent event) {
        if (doctor.getId() == null && event.id() != null) {
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
    }

    private void savePatientSafely(Patient patient, PatientRegisteredEvent event) {
        try {
            if (entityManager.contains(patient)) {
                return;
            }

            if (patient.getId() != null) {
                entityManager.persist(patient);
            } else {
                patientRepository.save(patient);
            }

        } catch (DataIntegrityViolationException ex) {
            log.warn("Patient already exists while syncing patient.registered. Retrying update for uhid={}",
                    event.uhid());

            Patient existing = findManagedPatient(event);
            applyPatient(existing, event);
        }
    }

    private void saveHospitalSafely(Hospital hospital, HospitalUpsertedEvent event) {
        try {
            if (entityManager.contains(hospital)) {
                return;
            }

            if (hospital.getId() != null) {
                entityManager.persist(hospital);
            } else {
                hospitalRepository.save(hospital);
            }

        } catch (DataIntegrityViolationException ex) {
            log.warn("Hospital already exists while syncing hospital.upserted. Retrying update for id={}",
                    event.id());

            Hospital existing = findManagedHospital(event.id());
            applyHospital(existing, event);
        }
    }

    private void saveDoctorSafely(Doctor doctor, DoctorRegisteredEvent event) {
        try {
            if (entityManager.contains(doctor)) {
                return;
            }

            if (doctor.getId() != null) {
                entityManager.persist(doctor);
            } else {
                doctorRepository.save(doctor);
            }

        } catch (DataIntegrityViolationException ex) {
            log.warn("Doctor already exists while syncing doctor.registered. Retrying update for id={}, email={}",
                    event.id(), event.email());

            Doctor existing = findManagedDoctor(event);
            applyDoctor(existing, event);
        }
    }

    private LocalDateTime parseDateTime(String value) {
        return value == null || value.isBlank() ? LocalDateTime.now() : LocalDateTime.parse(value);
    }
}