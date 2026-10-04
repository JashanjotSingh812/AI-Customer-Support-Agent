package com.example.demo.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String issue;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    protected Ticket() {
    }

    public Ticket(
            Long customerId,
            Long orderId,
            String issue,
            String status,
            LocalDateTime createdAt,
            String idempotencyKey
    ) {
        this.customerId = customerId;
        this.orderId = orderId;
        this.issue = issue;
        this.status = status;
        this.createdAt = createdAt;
        this.idempotencyKey = idempotencyKey;

    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getIssue() {
        return issue;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

}