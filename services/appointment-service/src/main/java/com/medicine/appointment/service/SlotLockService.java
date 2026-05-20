package com.medicine.appointment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class SlotLockService {

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    public SlotLockService(StringRedisTemplate redisTemplate,
                           @Value("${appointment.slot-lock.ttl-seconds:60}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    public boolean acquireSlotLock(Long doctorId, LocalDateTime appointmentTime) {
        String key = "slot:" + doctorId + ":" + appointmentTime;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, "LOCKED", ttl);
        return Boolean.TRUE.equals(locked);
    }
}
