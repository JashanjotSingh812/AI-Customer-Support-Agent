package com.example.demo.tools;

import com.example.demo.ai.tool.TicketTools;
import com.example.demo.entity.Ticket;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketToolsTest {

    private TicketService ticketService;
    private CurrentUserService currentUserService;
    private TicketTools ticketTools;

    @BeforeEach
    void setUp() {
        ticketService = mock(TicketService.class);
        currentUserService = mock(CurrentUserService.class);

        ticketTools = new TicketTools(
                ticketService,
                currentUserService
        );
    }

    @Test
    void shouldCreateTicketForAuthenticatedCustomer() {

        AuthenticatedUser user =
                new AuthenticatedUser(
                        501L,
                        "rahul@example.com"
                );

        Ticket ticket = new Ticket(
                501L,
                1001L,
                "Product arrived damaged",
                "OPEN",
                null,
                null
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(ticketService.createTicket(
                501L,
                1001L,
                "Product arrived damaged"
        )).thenReturn(ticket);

        Ticket result = ticketTools.createTicket(
                1001L,
                "Product arrived damaged"
        );

        assertNotNull(result);
        assertEquals(501L, result.getCustomerId());
        assertEquals(1001L, result.getOrderId());
        assertEquals("OPEN", result.getStatus());

        verify(currentUserService)
                .getCurrentUser();

        verify(ticketService)
                .createTicket(
                        501L,
                        1001L,
                        "Product arrived damaged"
                );
    }

    @Test
    void shouldUseAuthenticatedCustomerId() {

        AuthenticatedUser user =
                new AuthenticatedUser(
                        501L,
                        "rahul@example.com"
                );

        Ticket ticket = new Ticket(
                501L,
                1001L,
                "Wrong product received",
                "OPEN",
                null,
                null
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(ticketService.createTicket(
                501L,
                1001L,
                "Wrong product received"
        )).thenReturn(ticket);

        ticketTools.createTicket(
                1001L,
                "Wrong product received"
        );

        // Authenticated customer must be used
        verify(ticketService)
                .createTicket(
                        501L,
                        1001L,
                        "Wrong product received"
                );

        // Another customer's ID must never be used
        verify(ticketService, never())
                .createTicket(
                        502L,
                        1001L,
                        "Wrong product received"
                );
    }

    @Test
    void shouldPassOrderIdAndIssueCorrectly() {

        AuthenticatedUser user =
                new AuthenticatedUser(
                        502L,
                        "aman@example.com"
                );

        Ticket ticket = new Ticket(
                502L,
                1002L,
                "Laptop is not working",
                "OPEN",
                null,
                null
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(ticketService.createTicket(
                502L,
                1002L,
                "Laptop is not working"
        )).thenReturn(ticket);

        Ticket result = ticketTools.createTicket(
                1002L,
                "Laptop is not working"
        );

        assertNotNull(result);
        assertEquals(1002L, result.getOrderId());
        assertEquals(
                "Laptop is not working",
                result.getIssue()
        );

        verify(ticketService)
                .createTicket(
                        502L,
                        1002L,
                        "Laptop is not working"
                );
    }

    @Test
    void shouldPropagateExceptionFromTicketService() {

        AuthenticatedUser user =
                new AuthenticatedUser(
                        501L,
                        "rahul@example.com"
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(ticketService.createTicket(
                501L,
                1001L,
                "Product arrived damaged"
        )).thenThrow(
                new RuntimeException("Unable to create ticket")
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> ticketTools.createTicket(
                        1001L,
                        "Product arrived damaged"
                )
        );

        assertEquals(
                "Unable to create ticket",
                exception.getMessage()
        );

        verify(ticketService)
                .createTicket(
                        501L,
                        1001L,
                        "Product arrived damaged"
                );
    }
}
