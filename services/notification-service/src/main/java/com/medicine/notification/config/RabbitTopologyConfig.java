package com.medicine.notification.config;

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
public class RabbitTopologyConfig {

    @Bean
    public TopicExchange eventsExchange(@Value("${app.events.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue appointmentBookedQueue(@Value("${app.events.queues.appointment-booked}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Queue patientRegisteredQueue(@Value("${app.events.queues.patient-registered}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Queue otpRequestedQueue(@Value("${app.events.queues.otp-requested}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding appointmentBookedBinding(
            @Qualifier("appointmentBookedQueue") Queue appointmentBookedQueue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.appointment-booked}") String routingKey
    ) {
        return BindingBuilder.bind(appointmentBookedQueue).to(eventsExchange).with(routingKey);
    }

    @Bean
    public Binding patientRegisteredBinding(
            @Qualifier("patientRegisteredQueue") Queue patientRegisteredQueue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.patient-registered}") String routingKey
    ) {
        return BindingBuilder.bind(patientRegisteredQueue).to(eventsExchange).with(routingKey);
    }

    @Bean
    public Binding otpRequestedBinding(
            @Qualifier("otpRequestedQueue") Queue otpRequestedQueue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.otp-requested}") String routingKey
    ) {
        return BindingBuilder.bind(otpRequestedQueue).to(eventsExchange).with(routingKey);
    }
}
