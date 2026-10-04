package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.Ticket;
import com.example.demo.exception.BusinessRuleException;
import com.example.demo.kafka.TicketEventProducer;
import com.example.demo.observability.MetricsService;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final OrderRepository orderRepository;
    private final TicketEventProducer ticketEventProducer;
    private final MetricsService metricsService;

    private static final Logger log =
            LoggerFactory.getLogger(TicketService.class);

    public TicketService(
            TicketRepository ticketRepository, OrderRepository orderRepository, TicketEventProducer ticketEventProducer, MetricsService metricsService
    ) {
        this.ticketRepository = ticketRepository;
        this.orderRepository = orderRepository;
        this.ticketEventProducer = ticketEventProducer;
        this.metricsService = metricsService;
    }

    public Ticket createTicket(Long customerId, Long orderId, String issue) {

        log.info(
                "Creating ticket | customerId={} | orderId={}",
                customerId,
                orderId
        );

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn(
                            "Ticket creation failed | orderId={} | reason=order_not_found",
                            orderId
                    );

                    return new BusinessRuleException("Order not found");
                });

        if (!order.getCustomerId().equals(customerId)) {

            log.warn(
                    "Ticket creation rejected | orderId={} | customerId={} | reason=unauthorized",
                    orderId,
                    customerId
            );

            throw new BusinessRuleException(
                    "You are not authorized to create a ticket for this order"
            );
        }

        String idempotencyKey =
                customerId + ":" + orderId + ":" + issue;

        Optional<Ticket> existingTicket =
                ticketRepository.findByIdempotencyKey(idempotencyKey);

        if (existingTicket.isPresent()) {

            log.info(
                    "Existing ticket returned | ticketId={} | orderId={}",
                    existingTicket.get().getId(),
                    orderId
            );

            return existingTicket.get();
        }

        Ticket ticket = new Ticket(
                customerId,
                orderId,
                issue,
                "OPEN",
                LocalDateTime.now(),
                idempotencyKey
        );

        try {

            Ticket savedTicket = ticketRepository.save(ticket);

            log.info(
                    "Ticket saved to database | ticketId={} | orderId={}",
                    savedTicket.getId(),
                    orderId
            );

            metricsService.ticketCreated();

            ticketEventProducer.publishTicketCreated(savedTicket);

            log.info(
                    "Ticket creation completed | ticketId={}",
                    savedTicket.getId()
            );

            return savedTicket;

        } catch (DataIntegrityViolationException exception) {

            log.warn(
                    "Duplicate ticket detected | orderId={}",
                    orderId
            );

            return ticketRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() ->
                            new BusinessRuleException(
                                    "Unable to create support ticket"
                            ));
        }
    }
}