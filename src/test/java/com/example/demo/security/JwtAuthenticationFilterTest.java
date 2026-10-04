package com.example.demo.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtService);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateUserWithValidJwt() throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer valid-token");

        when(jwtService.extractClaims("valid-token"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn("rahul@example.com");

        when(claims.get("customerId", Long.class))
                .thenReturn(501L);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        assertNotNull(authentication);

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        assertEquals(501L, user.customerId());

        assertEquals(
                "rahul@example.com",
                user.username()
        );

        assertEquals(
                "ROLE_CUSTOMER",
                authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );

        verify(jwtService)
                .extractClaims("valid-token");

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsMissing()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(jwtService);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsNotBearer()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic username-password");

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(jwtService);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldClearContextWhenJwtIsInvalid()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer invalid-token");

        when(jwtService.extractClaims("invalid-token"))
                .thenThrow(
                        new RuntimeException("Invalid JWT")
                );

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(jwtService)
                .extractClaims("invalid-token");

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void shouldClearContextWhenCustomerIdIsMissing()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token-without-customer-id");

        when(jwtService.extractClaims(
                "token-without-customer-id"
        )).thenReturn(claims);

        when(claims.getSubject())
                .thenReturn("rahul@example.com");

        when(claims.get("customerId", Long.class))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }
}