package com.medicine.hospital.service;

import com.medicine.hospital.entity.Doctor;
import com.medicine.hospital.entity.Hospital;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class HospitalEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(HospitalEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String eventsExchange;
    private final String hospitalUpsertedRoutingKey;
    private final String doctorRegisteredRoutingKey;

    public HospitalEventPublisher(RabbitTemplate rabbitTemplate,
                                  ObjectMapper objectMapper,
                                  @Value("${app.events.exchange}") String eventsExchange,
                                  @Value("${app.events.routing-keys.hospital-upserted}") String hospitalUpsertedRoutingKey,
                                  @Value("${app.events.routing-keys.doctor-registered}") String doctorRegisteredRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.eventsExchange = eventsExchange;
        this.hospitalUpsertedRoutingKey = hospitalUpsertedRoutingKey;
        this.doctorRegisteredRoutingKey = doctorRegisteredRoutingKey;
    }

    public void publishHospitalUpserted(Hospital hospital) {
        HospitalUpsertedEvent event = new HospitalUpsertedEvent(
                hospital.getId(),
                hospital.getName(),
                hospital.getCity(),
                hospital.getAddress(),
                hospital.getPhone(),
                hospital.getEmail(),
                hospital.getImageUrls(),
                hospital.getRating(),
                hospital.getTotalReviews(),
                hospital.getServices(),
                hospital.getSpecializations(),
                hospital.getIsOpen24x7()
        );

        try {
            rabbitTemplate.convertAndSend(
                    eventsExchange,
                    hospitalUpsertedRoutingKey,
                    objectMapper.writeValueAsString(event)
            );
        } catch (Exception ex) {
            log.warn("Could not publish hospital.upserted event for {}", hospital.getId(), ex);
        }
    }

    public void publishDoctorRegistered(Doctor doctor) {
        DoctorRegisteredEvent event = new DoctorRegisteredEvent(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getExperience(),
                doctor.getFee(),
                doctor.getEmail(),
                doctor.getPassword(),
                doctor.getHospital() != null ? doctor.getHospital().getId() : null
        );

        try {
            rabbitTemplate.convertAndSend(
                    eventsExchange,
                    doctorRegisteredRoutingKey,
                    objectMapper.writeValueAsString(event)
            );
        } catch (Exception ex) {
            log.warn("Could not publish doctor.registered event for {}", doctor.getEmail(), ex);
        }
    }

    private record HospitalUpsertedEvent(
            String id,
            String name,
            String city,
            String address,
            String phone,
            String email,
            List<String> imageUrls,
            Double rating,
            Integer totalReviews,
            List<String> services,
            List<String> specializations,
            Boolean isOpen24x7
    ) {
    }

    private record DoctorRegisteredEvent(
            Long id,
            String name,
            String specialization,
            int experience,
            int fee,
            String email,
            String password,
            String hospitalId
    ) {
    }
}
