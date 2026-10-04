package com.example.demo.tools;

import com.example.demo.ai.tool.OrderTools;
import com.example.demo.entity.Order;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderToolsTest {

    private OrderService orderService;
    private CurrentUserService currentUserService;
    private OrderTools orderTools;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        currentUserService = mock(CurrentUserService.class);

        orderTools = new OrderTools(
                orderService,
                currentUserService
        );
    }

    @Test
    void shouldGetOwnOrder() {

        Long orderId = 1001L;

        AuthenticatedUser user =
                new AuthenticatedUser(
                        501L,
                        "rahul@example.com"
                );

        Order order = new Order(
                1001L,
                501L,
                "iPhone 17",
                "SHIPPED",
                BigDecimal.valueOf(79999)
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(orderService.getOrderForCustomer(
                1001L,
                501L
        )).thenReturn(order);

        Order result = orderTools.getOrder(orderId);

        assertNotNull(result);
        assertEquals(1001L, result.getId());
        assertEquals(501L, result.getCustomerId());
        assertEquals("SHIPPED", result.getStatus());

        verify(currentUserService)
                .getCurrentUser();

        verify(orderService)
                .getOrderForCustomer(1001L, 501L);
    }

    @Test
    void shouldUseAuthenticatedCustomerForOrderLookup() {

        AuthenticatedUser user =
                new AuthenticatedUser(
                        501L,
                        "rahul@example.com"
                );

        Order order = new Order(
                1001L,
                501L,
                "iPhone 17",
                "SHIPPED",
                BigDecimal.valueOf(79999)
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(orderService.getOrderForCustomer(
                1001L,
                501L
        )).thenReturn(order);

        orderTools.getOrder(1001L);

        // Customer ID must come from authenticated user
        verify(orderService)
                .getOrderForCustomer(1001L, 501L);

        // It must never use another customer's ID
        verify(orderService, never())
                .getOrderForCustomer(1001L, 502L);
    }

    @Test
    void shouldCancelOwnOrder() {

        AuthenticatedUser user =
                new AuthenticatedUser(
                        501L,
                        "rahul@example.com"
                );

        Order cancelledOrder = new Order(
                1001L,
                501L,
                "iPhone 17",
                "CANCELLED",
                BigDecimal.valueOf(79999)
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(orderService.cancelOrderForCustomer(
                1001L,
                501L
        )).thenReturn(cancelledOrder);

        Order result = orderTools.cancelOrder(1001L);

        assertNotNull(result);
        assertEquals(1001L, result.getId());
        assertEquals(501L, result.getCustomerId());
        assertEquals("CANCELLED", result.getStatus());

        verify(currentUserService)
                .getCurrentUser();

        verify(orderService)
                .cancelOrderForCustomer(1001L, 501L);
    }

    @Test
    void shouldUseAuthenticatedCustomerForCancellation() {

        AuthenticatedUser user =
                new AuthenticatedUser(
                        501L,
                        "rahul@example.com"
                );

        Order cancelledOrder = new Order(
                1002L,
                501L,
                "Dell Laptop",
                "CANCELLED",
                BigDecimal.valueOf(65000)
        );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(orderService.cancelOrderForCustomer(
                1002L,
                501L
        )).thenReturn(cancelledOrder);

        Order result = orderTools.cancelOrder(1002L);

        assertNotNull(result);
        assertEquals(1002L, result.getId());
        assertEquals(501L, result.getCustomerId());
        assertEquals("CANCELLED", result.getStatus());

        // Must use authenticated customer's ID
        verify(orderService)
                .cancelOrderForCustomer(1002L, 501L);

        // Must never use another customer's ID
        verify(orderService, never())
                .cancelOrderForCustomer(1002L, 502L);
    }

    @Test
    void shouldPropagateExceptionFromOrderService() {

        AuthenticatedUser user =
                new AuthenticatedUser(
                        501L,
                        "rahul@example.com"
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(orderService.cancelOrderForCustomer(
                1001L,
                501L
        )).thenThrow(
                new RuntimeException("Order cannot be cancelled")
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderTools.cancelOrder(1001L)
        );

        assertEquals(
                "Order cannot be cancelled",
                exception.getMessage()
        );

        verify(orderService)
                .cancelOrderForCustomer(1001L, 501L);
    }
}
