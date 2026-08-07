package com.medicine.patient.listener;

import com.medicine.patient.client.AppointmentReadModelClient;
import com.medicine.patient.dto.DoctorReadModelSnapshot;
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
public class PatientReadModelEventListener {

    private static final Logger log = LoggerFactory.getLogger(PatientReadModelEventListener.class);

    private final ObjectMapper objectMapper;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;
    private final AppointmentReadModelClient appointmentReadModelClient;

    public PatientReadModelEventListener(ObjectMapper objectMapper,
                                         AppointmentRepository appointmentRepository,
                                         PatientRepository patientRepository,
                                         HospitalRepository hospitalRepository,
                                         DoctorRepository doctorRepository,
                                         EntityManager entityManager,
                                         TransactionTemplate transactionTemplate,
                                         AppointmentReadModelClient appointmentReadModelClient) {
        this.objectMapper = objectMapper;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
        this.entityManager = entityManager;
        this.transactionTemplate = transactionTemplate;
        this.appointmentReadModelClient = appointmentReadModelClient;
    }

    @RabbitListener(queues = "${app.events.queues.patient-appointment-booked}")
    public void handleAppointmentBooked(String payload) {
        transactionTemplate.execute(status -> {
            try {
                AppointmentBookedEvent event = objectMapper.readValue(payload, AppointmentBookedEvent.class);
                if (event.appointmentId() == null) {
                    throw new IllegalArgumentException("appointment.booked event is missing appointmentId");
                }

                Optional<Patient> patient = findPatient(event);
                Optional<Hospital> hospital = event.hospitalId() == null
                        ? Optional.empty()
                        : hospitalRepository.findById(event.hospitalId());
                Optional<Doctor> doctor = resolveDoctorForAppointmentBooked(event);

                if (patient.isEmpty() || hospital.isEmpty() || doctor.isEmpty()) {
                    throw new IllegalStateException(
                            "Read-model dependency missing for appointment=" + event.appointmentId()
                                    + " patient=" + patient.isPresent()
                                    + " hospital=" + hospital.isPresent()
                                    + " doctor=" + doctor.isPresent()
                    );
                }

                Long appointmentId = event.appointmentId();
                if (appointmentRepository.existsById(appointmentId)) {
                    Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
                    appointment.setPatient(patient.get());
                    appointment.setHospital(hospital.get());
                    appointment.setDoctor(doctor.get());
                    appointment.setAppointmentTime(parseDateTime(event.appointmentTime()));
                    appointment.setCreatedAt(parseDateTime(event.createdAt()));
                    appointmentRepository.save(appointment);
                } else {
                    Appointment appointment = new Appointment();
                    appointment.setId(appointmentId);
                    appointment.setPatient(patient.get());
                    appointment.setHospital(hospital.get());
                    appointment.setDoctor(doctor.get());
                    appointment.setAppointmentTime(parseDateTime(event.appointmentTime()));
                    appointment.setCreatedAt(parseDateTime(event.createdAt()));
                    entityManager.persist(appointment);
                }

                log.info("Synced appointment {} into patient read model", appointmentId);
                return null;
            } catch (Exception ex) {
                status.setRollbackOnly();
                log.error("Failed to sync appointment.booked; message will be retried", ex);
                throw new RuntimeException("appointment.booked patient read-model sync failed", ex);
            }
        });
    }

    private Optional<Doctor> resolveDoctorForAppointmentBooked(AppointmentBookedEvent event) {
        if (event.doctorId() == null) {
            return Optional.empty();
        }

        Optional<Doctor> existing = doctorRepository.findById(event.doctorId());
        if (existing.isPresent()) {
            return existing;
        }

        Optional<DoctorReadModelSnapshot> snapshot = appointmentReadModelClient.fetchDoctorSnapshot(event.doctorId());
        if (snapshot.isEmpty()) {
            return Optional.empty();
        }

        DoctorReadModelSnapshot s = snapshot.get();
        try {
            Doctor doctor = new Doctor();
            doctor.setId(s.id());
            doctor.setEmail(s.email());
            doctor.setName(s.name());
            doctor.setSpecialization(s.specialization());
            doctor.setExperience(s.experience());
            doctor.setFee(s.fee());
            if (s.hospitalId() != null) {
                hospitalRepository.findById(s.hospitalId()).ifPresent(doctor::setHospital);
            }
            entityManager.persist(doctor);
            entityManager.flush();
            return doctorRepository.findById(event.doctorId());
        } catch (Exception ex) {
            log.warn("Failed to hydrate doctor {} from appointment-service", event.doctorId(), ex);
            return Optional.empty();
        }
    }

    @RabbitListener(queues = "${app.events.queues.patient-hospital-upserted}")
    public void handleHospitalUpserted(String payload) {
        transactionTemplate.execute(status -> {
            try {
                HospitalUpsertedEvent event = objectMapper.readValue(payload, HospitalUpsertedEvent.class);
                Hospital hospital = hospitalRepository.findById(event.id()).orElseGet(Hospital::new);
                applyHospital(hospital, event);
                hospitalRepository.save(hospital);
                log.info("Synced hospital {} into patient read model", event.id());
                return null;
            } catch (Exception ex) {
                status.setRollbackOnly();
                log.error("Failed to sync hospital.upserted; message will be retried", ex);
                throw new RuntimeException("hospital.upserted patient read-model sync failed", ex);
            }
        });
    }

    @RabbitListener(queues = "${app.events.queues.patient-doctor-registered}")
    public void handleDoctorRegistered(String payload) {
        transactionTemplate.execute(status -> {
            try {
                DoctorRegisteredEvent event = objectMapper.readValue(payload, DoctorRegisteredEvent.class);
                Long doctorId = event.id();
                if (doctorId == null) {
                    throw new IllegalArgumentException("doctor.registered event is missing id");
                }

                if (doctorRepository.existsById(doctorId)) {
                    Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();
                    applyDoctorFromEvent(doctor, event);
                    doctorRepository.save(doctor);
                } else {
                    Doctor doctor = new Doctor();
                    doctor.setId(doctorId);
                    applyDoctorFromEvent(doctor, event);
                    entityManager.persist(doctor);
                }

                log.info("Synced doctor {} (email={}) into patient read model", event.name(), event.email());
                return null;
            } catch (Exception ex) {
                status.setRollbackOnly();
                log.error("Failed to sync doctor.registered; message will be retried", ex);
                throw new RuntimeException("doctor.registered patient read-model sync failed", ex);
            }
        });
    }

    private void applyDoctorFromEvent(Doctor doctor, DoctorRegisteredEvent event) {
        doctor.setName(event.name());
        doctor.setSpecialization(event.specialization());
        doctor.setExperience(event.experience());
        doctor.setFee(event.fee());
        doctor.setEmail(event.email());
        if (event.hospitalId() != null) {
            hospitalRepository.findById(event.hospitalId()).ifPresent(doctor::setHospital);
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
