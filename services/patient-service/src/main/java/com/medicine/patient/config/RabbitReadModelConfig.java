package com.medicine.patient.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitReadModelConfig {

    @Bean
    public TopicExchange eventsExchange(@Value("${app.events.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue patientAppointmentBookedQueue(@Value("${app.events.queues.patient-appointment-booked}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Queue patientHospitalUpsertedQueue(@Value("${app.events.queues.patient-hospital-upserted}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Queue patientDoctorRegisteredQueue(@Value("${app.events.queues.patient-doctor-registered}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding patientAppointmentBookedBinding(
            @Qualifier("patientAppointmentBookedQueue") Queue queue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.appointment-booked}") String routingKey
    ) {
        return BindingBuilder.bind(queue).to(eventsExchange).with(routingKey);
    }

    @Bean
    public Binding patientHospitalUpsertedBinding(
            @Qualifier("patientHospitalUpsertedQueue") Queue queue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.hospital-upserted}") String routingKey
    ) {
        return BindingBuilder.bind(queue).to(eventsExchange).with(routingKey);
    }

    @Bean
    public Binding patientDoctorRegisteredBinding(
            @Qualifier("patientDoctorRegisteredQueue") Queue queue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.doctor-registered}") String routingKey
    ) {
        return BindingBuilder.bind(queue).to(eventsExchange).with(routingKey);
    }
}
