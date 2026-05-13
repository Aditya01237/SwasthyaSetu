package com.medicine.hospital.listener;

import com.medicine.hospital.entity.Doctor;
import com.medicine.hospital.event.DoctorRegisteredEvent;
import com.medicine.hospital.repository.DoctorRepository;
import com.medicine.hospital.repository.HospitalRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class HospitalReadModelEventListener {

    private static final Logger log = LoggerFactory.getLogger(HospitalReadModelEventListener.class);

    private final ObjectMapper objectMapper;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;

    public HospitalReadModelEventListener(ObjectMapper objectMapper,
                                          DoctorRepository doctorRepository,
                                          HospitalRepository hospitalRepository) {
        this.objectMapper = objectMapper;
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @Transactional
    @RabbitListener(queues = "${app.events.queues.hospital-doctor-registered}")
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
            log.error("Failed to sync doctor.registered into hospital read model", ex);
        }
    }

    private Doctor findDoctor(DoctorRegisteredEvent event) {
        if (event.id() != null) {
            return doctorRepository.findById(event.id()).orElseGet(() -> doctorRepository.findByEmail(event.email()).orElseGet(Doctor::new));
        }
        return doctorRepository.findByEmail(event.email()).orElseGet(Doctor::new);
    }
}
