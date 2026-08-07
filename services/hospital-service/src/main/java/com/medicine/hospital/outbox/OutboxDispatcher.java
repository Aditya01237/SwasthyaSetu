package com.medicine.hospital.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
public class OutboxDispatcher {
    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);
    private final OutboxEventRepository repository;
    private final RabbitTemplate rabbitTemplate;
    public OutboxDispatcher(OutboxEventRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository; this.rabbitTemplate = rabbitTemplate;
    }
    @Scheduled(fixedDelayString = "${app.outbox.poll-ms:1000}")
    @Transactional
    public void publishPending() {
        for (OutboxEvent event : repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                rabbitTemplate.convertAndSend(event.getExchangeName(), event.getRoutingKey(), event.getPayload());
                event.markPublished();
            } catch (Exception ex) {
                event.markFailed(ex);
                log.warn("Outbox publish failed for event {} routingKey={}", event.getId(), event.getRoutingKey(), ex);
            }
            repository.save(event);
        }
    }
}
