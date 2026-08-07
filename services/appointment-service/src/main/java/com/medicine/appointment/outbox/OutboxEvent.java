package com.medicine.appointment.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String exchangeName;

    @Column(nullable = false)
    private String routingKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private int attempts;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    protected OutboxEvent() {}

    public OutboxEvent(String exchangeName, String routingKey, String payload) {
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
        this.attempts = 0;
    }

    public Long getId() { return id; }
    public String getExchangeName() { return exchangeName; }
    public String getRoutingKey() { return routingKey; }
    public String getPayload() { return payload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public int getAttempts() { return attempts; }
    public String getLastError() { return lastError; }
    public void markPublished() { this.publishedAt = LocalDateTime.now(); this.lastError = null; }
    public void markFailed(Exception ex) {
        this.attempts++;
        String message = ex.getMessage();
        this.lastError = message == null ? ex.getClass().getSimpleName() : message.substring(0, Math.min(message.length(), 2000));
    }
}
