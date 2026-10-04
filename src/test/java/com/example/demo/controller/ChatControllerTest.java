package com.example.demo.controller;

import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.entity.Order;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.ChatService;
import com.example.demo.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private OrderService orderService;

    @Mock
    private CurrentUserService currentUserService;

    private ChatController chatController;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        chatController = new ChatController(
                chatService,
                orderService,
                currentUserService
        );
    }

    @Test
    void shouldReturnChatResponse() {

        ChatRequest request = new ChatRequest(
                "conv-1001",
                "Where is my order?"
        );

        when(chatService.chat(any(ChatRequest.class)))
                .thenReturn("Your order is currently shipped.");

        ChatResponse result = chatController.chat(request);

        assertNotNull(result);

        assertEquals(
                "Your order is currently shipped.",
                result.response()
        );

        verify(chatService)
                .chat(request);
    }

    @Test
    void shouldPassCorrectRequestToChatService() {

        ChatRequest request = new ChatRequest(
                "conv-2002",
                "Can you help me?"
        );

        when(chatService.chat(request))
                .thenReturn("I can help you with your order.");

        ChatResponse result = chatController.chat(request);

        assertNotNull(result);

        assertEquals(
                "I can help you with your order.",
                result.response()
        );

        verify(chatService)
                .chat(request);
    }

    @Test
    void shouldReturnOrderForCurrentCustomer() {

        Long orderId = 1001L;
        Long customerId = 501L;

        Order order = new Order(
                1001L,
                501L,
                "iPhone 17",
                "SHIPPED",
                BigDecimal.valueOf(79999)
        );

        AuthenticatedUser user =
                new AuthenticatedUser(
                        customerId,
                        "customer501"
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        when(orderService.getOrderForCustomer(
                orderId,
                customerId
        )).thenReturn(order);

        Order result = chatController.getOrder(orderId);

        assertNotNull(result);

        assertEquals(1001L, result.getId());
        assertEquals(501L, result.getCustomerId());
        assertEquals("iPhone 17", result.getProductName());
        assertEquals("SHIPPED", result.getStatus());

        assertEquals(
                BigDecimal.valueOf(79999),
                result.getAmount()
        );

        verify(currentUserService)
                .getCurrentUser();

        verify(orderService)
                .getOrderForCustomer(
                        orderId,
                        customerId
                );
    }
}