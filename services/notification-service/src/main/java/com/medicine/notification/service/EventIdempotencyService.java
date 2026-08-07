package com.medicine.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EventIdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(EventIdempotencyService.class);
    private static final String KEY_PREFIX = "notification:event:";

    private final StringRedisTemplate redisTemplate;
    private final Duration retention;

    public EventIdempotencyService(
            StringRedisTemplate redisTemplate,
            @Value("${app.notification.idempotency-ttl-hours:168}") long retentionHours) {
        this.redisTemplate = redisTemplate;
        this.retention = Duration.ofHours(retentionHours);
    }

    public void processOnce(String eventId, Runnable action) {
        // Older messages published before message IDs were introduced are still
        // processable; new outbox messages always carry a stable event ID.
        if (eventId == null || eventId.isBlank()) {
            action.run();
            return;
        }

        String key = KEY_PREFIX + eventId;
        Boolean claimed = redisTemplate.opsForValue()
                .setIfAbsent(key, "processing", retention);

        if (!Boolean.TRUE.equals(claimed)) {
            log.info("Skipping duplicate notification event {}", eventId);
            return;
        }

        try {
            action.run();
            redisTemplate.opsForValue().set(key, "done", retention);
        } catch (RuntimeException ex) {
            // Release a failed claim so RabbitMQ retry/DLQ semantics still work.
            redisTemplate.delete(key);
            throw ex;
        }
    }
}
