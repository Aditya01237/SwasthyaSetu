package com.medicine.appointment.config;

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
    public Queue appointmentPatientRegisteredQueue(@Value("${app.events.queues.appointment-patient-registered}") String queueName) {
        return durableWithDlq(queueName);
    }

    @Bean
    public Queue appointmentPatientRegisteredDlq(@Value("${app.events.queues.appointment-patient-registered}") String queueName) {
        return QueueBuilder.durable(queueName + ".dlq").build();
    }

    @Bean
    public Queue appointmentHospitalUpsertedQueue(@Value("${app.events.queues.appointment-hospital-upserted}") String queueName) {
        return durableWithDlq(queueName);
    }

    @Bean
    public Queue appointmentHospitalUpsertedDlq(@Value("${app.events.queues.appointment-hospital-upserted}") String queueName) {
        return QueueBuilder.durable(queueName + ".dlq").build();
    }

    @Bean
    public Queue appointmentDoctorRegisteredQueue(@Value("${app.events.queues.appointment-doctor-registered}") String queueName) {
        return durableWithDlq(queueName);
    }

    @Bean
    public Queue appointmentDoctorRegisteredDlq(@Value("${app.events.queues.appointment-doctor-registered}") String queueName) {
        return QueueBuilder.durable(queueName + ".dlq").build();
    }

    @Bean
    public Binding appointmentPatientRegisteredBinding(
            @Qualifier("appointmentPatientRegisteredQueue") Queue queue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.patient-registered}") String routingKey) {
        return BindingBuilder.bind(queue).to(eventsExchange).with(routingKey);
    }

    @Bean
    public Binding appointmentHospitalUpsertedBinding(
            @Qualifier("appointmentHospitalUpsertedQueue") Queue queue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.hospital-upserted}") String routingKey) {
        return BindingBuilder.bind(queue).to(eventsExchange).with(routingKey);
    }

    @Bean
    public Binding appointmentDoctorRegisteredBinding(
            @Qualifier("appointmentDoctorRegisteredQueue") Queue queue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.doctor-registered}") String routingKey) {
        return BindingBuilder.bind(queue).to(eventsExchange).with(routingKey);
    }

    private Queue durableWithDlq(String queueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange("")
                .deadLetterRoutingKey(queueName + ".dlq")
                .build();
    }
}
