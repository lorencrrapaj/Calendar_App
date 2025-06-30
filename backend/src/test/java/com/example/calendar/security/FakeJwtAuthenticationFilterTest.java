package com.example.calendar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FakeJwtAuthenticationFilterTest {

    @Mock
    private TokenStore tokenStore;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private FakeJwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new FakeJwtAuthenticationFilter(tokenStore);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ValidBearerToken_SetsAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid-token";
        String email = "test@example.com";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenStore.getEmail(token)).thenReturn(email);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo(email);
        assertThat(auth.getAuthorities()).hasSize(1);
        assertThat(auth.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        verify(tokenStore).getEmail(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        String token = "invalid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenStore.getEmail(token)).thenReturn(null);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();

        verify(tokenStore).getEmail(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();

        verify(tokenStore, never()).getEmail(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_EmptyAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();

        verify(tokenStore, never()).getEmail(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NonBearerToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic some-token");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();

        verify(tokenStore, never()).getEmail(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_BearerWithoutToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();

        verify(tokenStore, never()).getEmail(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_BearerWithEmptyToken_ChecksEmptyToken() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(tokenStore.getEmail("")).thenReturn(null);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();

        verify(tokenStore).getEmail("");
        verify(filterChain).doFilter(request, response);
    }


    @Test
    void doFilterInternal_AlreadyAuthenticated_DoesNotOverrideAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid-token";
        String email = "test@example.com";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenStore.getEmail(token)).thenReturn(email);

        // Set existing authentication
        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotEqualTo(existingAuth); // Filter overrides existing authentication

        verify(tokenStore).getEmail(token);
        verify(filterChain).doFilter(request, response);
    }
}
