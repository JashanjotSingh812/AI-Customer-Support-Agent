package com.example.demo.ai.tool;

import com.example.demo.entity.Ticket;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class TicketTools {

    private final TicketService ticketService;
    private final CurrentUserService currentUserService;

    private static final Logger log =
            LoggerFactory.getLogger(TicketTools.class);

    public TicketTools(
            TicketService ticketService,
            CurrentUserService currentUserService
    ) {
        this.ticketService = ticketService;
        this.currentUserService = currentUserService;
    }

    @Tool(description = """
        Create a support ticket for an order.
        Use this when the customer explicitly reports an issue
        that requires support.
        The order must belong to the authenticated customer.
        """)
    public Ticket createTicket(Long orderId, String issue) {

        AuthenticatedUser user = currentUserService.getCurrentUser();

        log.info(
                "Ticket creation requested | orderId={} | customerId={}",
                orderId,
                user.customerId()
        );

        Ticket ticket = ticketService.createTicket(
                user.customerId(),
                orderId,
                issue
        );

        log.info(
                "Ticket created | ticketId={} | orderId={} | customerId={} | status={}",
                ticket.getId(),
                ticket.getOrderId(),
                ticket.getCustomerId(),
                ticket.getStatus()
        );

        return ticket;
    }
}