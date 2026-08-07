package com.medicine.patient.outbox;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 64) private String eventId;
    @Column(nullable = false) private String exchangeName;
    @Column(nullable = false) private String routingKey;
    @Column(nullable = false, columnDefinition = "TEXT") private String payload;
    @Column(nullable = false) private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    @Column(nullable = false) private int attempts;
    @Column(columnDefinition = "TEXT") private String lastError;
    protected OutboxEvent() {}
    public OutboxEvent(String exchangeName, String routingKey, String payload) {
        this.eventId = UUID.randomUUID().toString();
        this.exchangeName = exchangeName; this.routingKey = routingKey; this.payload = payload;
        this.createdAt = LocalDateTime.now(); this.attempts = 0;
    }
    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getExchangeName() { return exchangeName; }
    public String getRoutingKey() { return routingKey; }
    public String getPayload() { return payload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public int getAttempts() { return attempts; }
    public String getLastError() { return lastError; }
    public void markPublished() { publishedAt = LocalDateTime.now(); lastError = null; }
    public void markFailed(Exception ex) { attempts++; String m = ex.getMessage(); lastError = m == null ? ex.getClass().getSimpleName() : m.substring(0, Math.min(m.length(), 2000)); }
}
