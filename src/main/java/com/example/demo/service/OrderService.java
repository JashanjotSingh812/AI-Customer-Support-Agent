package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.exception.BusinessRuleException;
import com.example.demo.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order getOrder(Long orderId) {

        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new BusinessRuleException(
                                "Order does not exist."
                        )
                );
    }


    public Order getOrderForCustomer(
            Long orderId,
            Long customerId
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new BusinessRuleException(
                                "Order does not exist."
                        )
                );

        if (!order.getCustomerId().equals(customerId)) {
            throw new BusinessRuleException(
                    "You are not authorized to access this order."
            );
        }

        return order;
    }


    public Order cancelOrderForCustomer(Long orderId, Long customerId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new BusinessRuleException("Order not found")
                );

        if (!order.getCustomerId().equals(customerId)) {
            throw new BusinessRuleException(
                    "You are not authorized to access this order"
            );
        }

        if ("SHIPPED".equals(order.getStatus())) {
            throw new BusinessRuleException(
                    "Shipped orders cannot be cancelled"
            );
        }

        if ("DELIVERED".equals(order.getStatus())) {
            throw new BusinessRuleException(
                    "Delivered orders cannot be cancelled"
            );
        }

        if ("CANCELLED".equals(order.getStatus())) {
            throw new BusinessRuleException(
                    "Order is already cancelled"
            );
        }

        order.setStatus("CANCELLED");

        return orderRepository.save(order);
    }

}