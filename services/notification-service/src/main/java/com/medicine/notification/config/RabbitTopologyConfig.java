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
        return durableWithDlq(queueName);
    }

    @Bean
    public Queue appointmentBookedDlq(@Value("${app.events.queues.appointment-booked}") String queueName) {
        return QueueBuilder.durable(queueName + ".dlq").build();
    }

    @Bean
    public Queue patientRegisteredQueue(@Value("${app.events.queues.patient-registered}") String queueName) {
        return durableWithDlq(queueName);
    }

    @Bean
    public Queue patientRegisteredDlq(@Value("${app.events.queues.patient-registered}") String queueName) {
        return QueueBuilder.durable(queueName + ".dlq").build();
    }

    @Bean
    public Queue otpRequestedQueue(@Value("${app.events.queues.otp-requested}") String queueName) {
        return durableWithDlq(queueName);
    }

    @Bean
    public Queue otpRequestedDlq(@Value("${app.events.queues.otp-requested}") String queueName) {
        return QueueBuilder.durable(queueName + ".dlq").build();
    }

    @Bean
    public Binding appointmentBookedBinding(
            @Qualifier("appointmentBookedQueue") Queue queue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.appointment-booked}") String routingKey) {
        return BindingBuilder.bind(queue).to(eventsExchange).with(routingKey);
    }

    @Bean
    public Binding patientRegisteredBinding(
            @Qualifier("patientRegisteredQueue") Queue queue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.patient-registered}") String routingKey) {
        return BindingBuilder.bind(queue).to(eventsExchange).with(routingKey);
    }

    @Bean
    public Binding otpRequestedBinding(
            @Qualifier("otpRequestedQueue") Queue queue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.otp-requested}") String routingKey) {
        return BindingBuilder.bind(queue).to(eventsExchange).with(routingKey);
    }

    private Queue durableWithDlq(String queueName) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange("")
                .deadLetterRoutingKey(queueName + ".dlq")
                .build();
    }
}
