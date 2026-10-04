package com.example.demo.config;

import com.example.demo.entity.Customer;
import com.example.demo.entity.Order;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            OrderRepository orderRepository,
            CustomerRepository customerRepository
    ) {

        return args -> {

            // -------------------------
            // Customers
            // -------------------------

            if (customerRepository.count() == 0) {

                customerRepository.save(
                        new Customer(
                                501L,
                                "Rahul Sharma",
                                "rahul@example.com",
                                "9876543210"
                        )
                );

                customerRepository.save(
                        new Customer(
                                502L,
                                "Aman Verma",
                                "aman@example.com",
                                "9876543211"
                        )
                );
            }

            // -------------------------
            // Orders
            // -------------------------

            if (orderRepository.count() == 0) {

                orderRepository.save(
                        new Order(
                                1001L,
                                501L,
                                "iPhone 17",
                                "SHIPPED",
                                new BigDecimal("79999")
                        )
                );

                orderRepository.save(
                        new Order(
                                1002L,
                                502L,
                                "Dell Laptop",
                                "DELIVERED",
                                new BigDecimal("65000")
                        )
                );

                orderRepository.save(
                        new Order(
                                1003L,
                                501L,
                                "Sony Headphones",
                                "CANCELLED",
                                new BigDecimal("12000")
                        )
                );
            }
        };
    }
}