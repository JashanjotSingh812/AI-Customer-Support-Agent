package com.example.demo.service;

import com.example.demo.exception.BusinessRuleException;
import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;


    // ============================================================
    // TEST 1
    // Customer can fetch their own order
    // ============================================================

    @Test
    void shouldReturnOrderWhenCustomerOwnsOrder() {

        // Arrange
        Long orderId = 1001L;
        Long customerId = 501L;

        Order order = new Order(
                1001L,
                501L,
                "iPhone 17",
                "SHIPPED",
                BigDecimal.valueOf(79999.0)
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act
        Order result = orderService.getOrderForCustomer(
                orderId,
                customerId
        );

        // Assert
        assertEquals(order, result);

        verify(orderRepository)
                .findById(orderId);
    }


    // ============================================================
    // TEST 2
    // Customer cannot access another customer's order
    // ============================================================

    @Test
    void shouldRejectOrderAccessWhenCustomerDoesNotOwnOrder() {

        // Arrange
        Long orderId = 1002L;
        Long customerId = 501L;

        Order order = new Order(
                1002L,
                502L,
                "Dell Laptop",
                "DELIVERED",
                BigDecimal.valueOf(65000.0)
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act + Assert
        assertThrows(
                BusinessRuleException.class,
                () -> orderService.getOrderForCustomer(
                        orderId,
                        customerId
                )
        );
    }


    // ============================================================
    // TEST 3
    // Order does not exist
    // ============================================================

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        // Arrange
        Long orderId = 9999L;
        Long customerId = 501L;

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                BusinessRuleException.class,
                () -> orderService.getOrderForCustomer(
                        orderId,
                        customerId
                )
        );
    }


    // ============================================================
    // TEST 4
    // Customer can cancel an allowed order
    // ============================================================

    @Test
    void shouldCancelOrderSuccessfully() {

        // Arrange
        Long orderId = 1001L;
        Long customerId = 501L;

        Order order = new Order(
                1001L,
                501L,
                "iPhone 17",
                "PLACED",
                BigDecimal.valueOf(79999.0)
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
                .thenReturn(order);

        // Act
        Order result = orderService.cancelOrderForCustomer(
                orderId,
                customerId
        );

        // Assert
        assertEquals("CANCELLED", result.getStatus());

        verify(orderRepository)
                .save(order);
    }


    // ============================================================
    // TEST 5
    // SHIPPED order cannot be cancelled
    // ============================================================

    @Test
    void shouldRejectCancellationWhenOrderIsShipped() {

        // Arrange
        Long orderId = 1001L;
        Long customerId = 501L;

        Order order = new Order(
                1001L,
                501L,
                "iPhone 17",
                "SHIPPED",
                BigDecimal.valueOf(79999.0)
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act + Assert
        assertThrows(
                BusinessRuleException.class,
                () -> orderService.cancelOrderForCustomer(
                        orderId,
                        customerId
                )
        );
    }


    // ============================================================
    // TEST 6
    // DELIVERED order cannot be cancelled
    // ============================================================

    @Test
    void shouldRejectCancellationWhenOrderIsDelivered() {

        // Arrange
        Long orderId = 1002L;
        Long customerId = 502L;

        Order order = new Order(
                1002L,
                502L,
                "Dell Laptop",
                "DELIVERED",
                BigDecimal.valueOf(65000.0)
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act + Assert
        assertThrows(
                BusinessRuleException.class,
                () -> orderService.cancelOrderForCustomer(
                        orderId,
                        customerId
                )
        );
    }


    // ============================================================
    // TEST 7
    // Already cancelled order cannot be cancelled again
    // ============================================================

    @Test
    void shouldRejectCancellationWhenOrderIsAlreadyCancelled() {

        // Arrange
        Long orderId = 1003L;
        Long customerId = 501L;

        Order order = new Order(
                1003L,
                501L,
                "Sony Headphones",
                "CANCELLED",
                BigDecimal.valueOf(12000.0)
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // Act + Assert
        assertThrows(
                BusinessRuleException.class,
                () -> orderService.cancelOrderForCustomer(
                        orderId,
                        customerId
                )
        );
    }
}
