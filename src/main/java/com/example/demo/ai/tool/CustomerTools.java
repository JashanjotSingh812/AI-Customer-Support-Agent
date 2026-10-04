package com.example.demo.ai.tool;

import com.example.demo.entity.Customer;
import com.example.demo.service.CustomerService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class CustomerTools {

    private final CustomerService customerService;

    public CustomerTools(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Tool(description = "Get customer details using the customer ID")
    public Customer getCustomer(Long customerId) {

        return customerService.getCustomer(customerId);
    }
}