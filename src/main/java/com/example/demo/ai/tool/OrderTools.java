package com.example.demo.ai.tool;

import com.example.demo.entity.Order;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.security.CurrentUserService;
import com.example.demo.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class OrderTools {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    private static final Logger log =
            LoggerFactory.getLogger(OrderTools.class);

    public OrderTools(
            OrderService orderService,
            CurrentUserService currentUserService
    ) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @Tool(description = """
        Get an order by ID.
        The order must belong to the authenticated customer.
        """)
    public Order getOrder(Long orderId) {

        AuthenticatedUser user = currentUserService.getCurrentUser();

        log.info(
                "Order lookup requested | orderId={} | customerId={}",
                orderId,
                user.customerId()
        );

        Order order = orderService.getOrderForCustomer(
                orderId,
                user.customerId()
        );

        log.info(
                "Order lookup completed | orderId={} | status={}",
                orderId,
                order.getStatus()
        );

        return order;
    }

    @Tool(description = """
        Cancel an order.
        Use this only when the customer explicitly asks to cancel.
        The order must belong to the authenticated customer.
        """)
    public Order cancelOrder(Long orderId) {

        AuthenticatedUser user = currentUserService.getCurrentUser();

        log.info(
                "Order cancellation requested | orderId={} | customerId={}",
                orderId,
                user.customerId()
        );

        Order order = orderService.cancelOrderForCustomer(
                orderId,
                user.customerId()
        );

        log.info(
                "Order cancellation completed | orderId={} | status={}",
                orderId,
                order.getStatus()
        );

        return order;
    }
}