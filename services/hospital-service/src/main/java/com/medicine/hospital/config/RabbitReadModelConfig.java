package com.medicine.hospital.config;

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
    public Queue hospitalDoctorRegisteredQueue(@Value("${app.events.queues.hospital-doctor-registered}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding hospitalDoctorRegisteredBinding(
            @Qualifier("hospitalDoctorRegisteredQueue") Queue queue,
            TopicExchange eventsExchange,
            @Value("${app.events.routing-keys.doctor-registered}") String routingKey
    ) {
        return BindingBuilder.bind(queue).to(eventsExchange).with(routingKey);
    }
}
