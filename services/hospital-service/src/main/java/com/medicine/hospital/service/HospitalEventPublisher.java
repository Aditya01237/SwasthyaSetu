package com.medicine.hospital.service;

import com.medicine.hospital.entity.Doctor;
import com.medicine.hospital.entity.Hospital;
import com.medicine.hospital.outbox.OutboxEvent;
import com.medicine.hospital.outbox.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class HospitalEventPublisher {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final String eventsExchange;
    private final String hospitalUpsertedRoutingKey;
    private final String doctorRegisteredRoutingKey;

    public HospitalEventPublisher(OutboxEventRepository outboxRepository,
                                  ObjectMapper objectMapper,
                                  @Value("${app.events.exchange}") String eventsExchange,
                                  @Value("${app.events.routing-keys.hospital-upserted}") String hospitalUpsertedRoutingKey,
                                  @Value("${app.events.routing-keys.doctor-registered}") String doctorRegisteredRoutingKey) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.eventsExchange = eventsExchange;
        this.hospitalUpsertedRoutingKey = hospitalUpsertedRoutingKey;
        this.doctorRegisteredRoutingKey = doctorRegisteredRoutingKey;
    }

    public void publishHospitalUpserted(Hospital hospital) {
        HospitalUpsertedEvent event = new HospitalUpsertedEvent(
                hospital.getId(), hospital.getName(), hospital.getCity(), hospital.getAddress(), hospital.getPhone(),
                hospital.getEmail(), hospital.getImageUrls(), hospital.getRating(), hospital.getTotalReviews(),
                hospital.getServices(), hospital.getSpecializations(), hospital.getIsOpen24x7()
        );
        enqueue(hospitalUpsertedRoutingKey, event, "hospital.upserted");
    }

    public void publishDoctorRegistered(Doctor doctor) {
        DoctorRegisteredEvent event = new DoctorRegisteredEvent(
                doctor.getId(), doctor.getName(), doctor.getSpecialization(), doctor.getExperience(), doctor.getFee(),
                doctor.getEmail(), doctor.getHospital() != null ? doctor.getHospital().getId() : null
        );
        enqueue(doctorRegisteredRoutingKey, event, "doctor.registered");
    }

    private void enqueue(String routingKey, Object event, String eventName) {
        try {
            outboxRepository.save(new OutboxEvent(eventsExchange, routingKey, objectMapper.writeValueAsString(event)));
        } catch (Exception ex) {
            throw new RuntimeException("Could not persist " + eventName + " outbox event", ex);
        }
    }

    private record HospitalUpsertedEvent(String id, String name, String city, String address, String phone,
                                         String email, List<String> imageUrls, Double rating, Integer totalReviews,
                                         List<String> services, List<String> specializations, Boolean isOpen24x7) {}
    private record DoctorRegisteredEvent(Long id, String name, String specialization, int experience, int fee,
                                         String email, String hospitalId) {}
}
