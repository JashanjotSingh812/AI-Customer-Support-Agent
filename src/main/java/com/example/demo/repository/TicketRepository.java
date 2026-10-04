package com.example.demo.repository;

import com.example.demo.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository
        extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByIdempotencyKey(String idempotencyKey);
}