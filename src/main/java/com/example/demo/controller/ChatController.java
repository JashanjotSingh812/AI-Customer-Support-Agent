package com.example.demo.controller;

import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.entity.Order;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.ChatService;
import com.example.demo.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final OrderService orderService;
    private final CurrentUserService currentUserService;


    public ChatController(ChatService chatService, OrderService orderService, CurrentUserService currentUserService) {
        this.chatService = chatService;
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String response = chatService.chat(request);

        return new ChatResponse(response);
    }

    @GetMapping("/orders/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {

        AuthenticatedUser user =
                currentUserService.getCurrentUser();

        return orderService.getOrderForCustomer(
                orderId,
                user.customerId()
        );
    }
}