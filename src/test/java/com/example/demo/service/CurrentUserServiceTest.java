package com.example.demo.service;

import com.example.demo.security.AuthenticatedUser;
import com.example.demo.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserServiceTest {

    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        currentUserService = new CurrentUserService();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnAuthenticatedUser() {

        AuthenticatedUser user =
                new AuthenticatedUser(501L, "rahul@example.com");

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        AuthenticatedUser result =
                currentUserService.getCurrentUser();

        assertEquals(501L, result.customerId());
        assertEquals("rahul@example.com", result.username());
    }

    @Test
    void shouldRejectWhenUserIsNotAuthenticated() {

        SecurityContextHolder.clearContext();

        assertThrows(
                Exception.class,
                () -> currentUserService.getCurrentUser()
        );
    }

    @Test
    void shouldRejectInvalidPrincipal() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "invalid-user",
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                Exception.class,
                () -> currentUserService.getCurrentUser()
        );
    }
}
