package com.example.demo.service;

import com.example.demo.entity.Customer;
import com.example.demo.exception.BusinessRuleException;
import com.example.demo.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(
            CustomerRepository customerRepository
    ) {
        this.customerRepository = customerRepository;
    }

    public Customer getCustomer(Long customerId) {

        return customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new BusinessRuleException(
                                "Customer does not exist."
                        )
                );
    }
}