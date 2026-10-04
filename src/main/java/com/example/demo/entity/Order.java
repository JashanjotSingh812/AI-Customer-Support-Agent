package com.example.demo.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private BigDecimal amount;

    protected Order() {
    }

    public Order(
            Long id,
            Long customerId,
            String productName,
            String status,
            BigDecimal amount
    ) {
        this.id = id;
        this.customerId = customerId;
        this.productName = productName;
        this.status = status;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getProductName() {
        return productName;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}