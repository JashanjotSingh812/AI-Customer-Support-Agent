package com.example.demo.tools;

import com.example.demo.ai.tool.CustomerTools;
import com.example.demo.entity.Customer;
import com.example.demo.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerToolsTest {

    @Mock
    private CustomerService customerService;

    private CustomerTools customerTools;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customerTools = new CustomerTools(customerService);
    }

    @Test
    void shouldReturnCustomer() {

        Long customerId = 501L;

        Customer customer = new Customer(
                501L,
                "Rahul Sharma",
                "rahul@example.com",
                "9876543210"
        );

        when(customerService.getCustomer(customerId))
                .thenReturn(customer);

        Customer result =
                customerTools.getCustomer(customerId);

        assertNotNull(result);

        assertEquals(
                501L,
                result.getId()
        );

        assertEquals(
                "Rahul Sharma",
                result.getName()
        );

        assertEquals(
                "rahul@example.com",
                result.getEmail()
        );

        assertEquals(
                "9876543210",
                result.getPhone()
        );

        verify(customerService)
                .getCustomer(customerId);
    }

    @Test
    void shouldPassCorrectCustomerIdToService() {

        Long customerId = 502L;

        Customer customer = new Customer(
                502L,
                "Aman Verma",
                "aman@example.com",
                "9876543211"
        );

        when(customerService.getCustomer(502L))
                .thenReturn(customer);

        Customer result =
                customerTools.getCustomer(502L);

        assertNotNull(result);

        assertEquals(
                502L,
                result.getId()
        );

        verify(customerService)
                .getCustomer(502L);

        verify(customerService, never())
                .getCustomer(501L);
    }

    @Test
    void shouldPropagateExceptionFromCustomerService() {

        Long customerId = 501L;

        when(customerService.getCustomer(customerId))
                .thenThrow(
                        new RuntimeException("Customer not found")
                );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> customerTools.getCustomer(customerId)
        );

        assertEquals(
                "Customer not found",
                exception.getMessage()
        );

        verify(customerService)
                .getCustomer(customerId);
    }

    @Test
    void shouldReturnNullWhenServiceReturnsNull() {

        Long customerId = 501L;

        when(customerService.getCustomer(customerId))
                .thenReturn(null);

        Customer result =
                customerTools.getCustomer(customerId);

        assertNull(result);

        verify(customerService)
                .getCustomer(customerId);
    }
}
