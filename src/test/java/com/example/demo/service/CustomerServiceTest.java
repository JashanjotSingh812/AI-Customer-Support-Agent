package com.example.demo.service;

import com.example.demo.entity.Customer;
import com.example.demo.exception.BusinessRuleException;
import com.example.demo.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customerService =
                new CustomerService(customerRepository);
    }

    @Test
    void shouldReturnCustomerWhenCustomerExists() {

        Customer customer = new Customer(
                501L,
                "Rahul Sharma",
                "rahul@example.com",
                "9876543210"
        );

        when(customerRepository.findById(501L))
                .thenReturn(Optional.of(customer));

        Customer result =
                customerService.getCustomer(501L);

        assertNotNull(result);

        assertEquals(501L, result.getId());
        assertEquals("Rahul Sharma", result.getName());
        assertEquals("rahul@example.com", result.getEmail());
        assertEquals("9876543210", result.getPhone());

        verify(customerRepository)
                .findById(501L);
    }

    @Test
    void shouldThrowExceptionWhenCustomerDoesNotExist() {

        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());

        BusinessRuleException exception =
                assertThrows(
                        BusinessRuleException.class,
                        () -> customerService.getCustomer(999L)
                );

        assertEquals(
                "Customer does not exist.",
                exception.getMessage()
        );

        verify(customerRepository)
                .findById(999L);
    }

    @Test
    void shouldUseCorrectCustomerId() {

        Customer customer = new Customer(
                502L,
                "Aman Verma",
                "aman@example.com",
                "9876543211"
        );

        when(customerRepository.findById(502L))
                .thenReturn(Optional.of(customer));

        Customer result =
                customerService.getCustomer(502L);

        assertEquals(502L, result.getId());

        verify(customerRepository)
                .findById(502L);

        verify(customerRepository, never())
                .findById(501L);
    }
}