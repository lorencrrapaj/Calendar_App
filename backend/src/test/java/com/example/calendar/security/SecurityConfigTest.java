package com.example.calendar.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private TokenStore tokenStore;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(tokenStore);
    }

    @Test
    void passwordEncoder_ReturnsBCryptPasswordEncoder() {
        // When
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Then
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void passwordEncoder_EncodesPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    void passwordEncoder_DifferentEncodingsForSamePassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String rawPassword = "testPassword123";

        // When
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);

        // Then
        assertThat(encoded1).isNotEqualTo(encoded2); // BCrypt uses salt
        assertThat(passwordEncoder.matches(rawPassword, encoded1)).isTrue();
        assertThat(passwordEncoder.matches(rawPassword, encoded2)).isTrue();
    }

    @Test
    void filterChain_ReturnsSecurityFilterChain() throws Exception {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        // When
        SecurityFilterChain filterChain = securityConfig.filterChain(httpSecurity);

        // Then
        assertThat(filterChain).isNotNull();
    }

    @Test
    void corsConfig_ReturnsValidCorsConfigurationSource() {
        // When
        CorsConfigurationSource corsConfigSource = securityConfig.corsConfig();

        // Then
        assertThat(corsConfigSource).isInstanceOf(UrlBasedCorsConfigurationSource.class);
    }

    @Test
    void corsConfig_HasCorrectConfiguration() {
        // When
        CorsConfigurationSource corsConfigSource = securityConfig.corsConfig();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigSource;
        CorsConfiguration corsConfig = urlBasedSource.getCorsConfigurations().get("/**");

        // Then
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedOrigins()).contains("http://localhost:5173");
        assertThat(corsConfig.getAllowedMethods()).containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(corsConfig.getAllowedHeaders()).contains("*");
        assertThat(corsConfig.getAllowCredentials()).isTrue();
    }

    @Test
    void corsConfig_AllowsSpecificOrigin() {
        // When
        CorsConfigurationSource corsConfigSource = securityConfig.corsConfig();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigSource;
        CorsConfiguration corsConfig = urlBasedSource.getCorsConfigurations().get("/**");

        // Then
        assertThat(corsConfig.getAllowedOrigins()).hasSize(1);
        assertThat(corsConfig.getAllowedOrigins().get(0)).isEqualTo("http://localhost:5173");
    }

    @Test
    void corsConfig_AllowsAllHeaders() {
        // When
        CorsConfigurationSource corsConfigSource = securityConfig.corsConfig();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigSource;
        CorsConfiguration corsConfig = urlBasedSource.getCorsConfigurations().get("/**");

        // Then
        assertThat(corsConfig.getAllowedHeaders()).hasSize(1);
        assertThat(corsConfig.getAllowedHeaders().get(0)).isEqualTo("*");
    }

    @Test
    void corsConfig_AllowsCredentials() {
        // When
        CorsConfigurationSource corsConfigSource = securityConfig.corsConfig();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigSource;
        CorsConfiguration corsConfig = urlBasedSource.getCorsConfigurations().get("/**");

        // Then
        assertThat(corsConfig.getAllowCredentials()).isTrue();
    }

    @Test
    void corsConfig_AllowsRequiredHttpMethods() {
        // When
        CorsConfigurationSource corsConfigSource = securityConfig.corsConfig();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsConfigSource;
        CorsConfiguration corsConfig = urlBasedSource.getCorsConfigurations().get("/**");

        // Then
        assertThat(corsConfig.getAllowedMethods()).hasSize(5);
        assertThat(corsConfig.getAllowedMethods()).contains("GET");
        assertThat(corsConfig.getAllowedMethods()).contains("POST");
        assertThat(corsConfig.getAllowedMethods()).contains("PUT");
        assertThat(corsConfig.getAllowedMethods()).contains("DELETE");
        assertThat(corsConfig.getAllowedMethods()).contains("OPTIONS");
    }

    @Test
    void passwordEncoder_HandlesEmptyPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String emptyPassword = "";

        // When
        String encodedPassword = passwordEncoder.encode(emptyPassword);

        // Then
        assertThat(encodedPassword).isNotEmpty();
        assertThat(passwordEncoder.matches(emptyPassword, encodedPassword)).isTrue();
    }

    @Test
    void passwordEncoder_HandlesSpecialCharacters() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String specialPassword = "p@ssw0rd!#$%^&*()";

        // When
        String encodedPassword = passwordEncoder.encode(specialPassword);

        // Then
        assertThat(encodedPassword).isNotEqualTo(specialPassword);
        assertThat(passwordEncoder.matches(specialPassword, encodedPassword)).isTrue();
    }

    @Test
    void passwordEncoder_RejectsWrongPassword() {
        // Given
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";

        // When
        String encodedPassword = passwordEncoder.encode(correctPassword);

        // Then
        assertThat(passwordEncoder.matches(wrongPassword, encodedPassword)).isFalse();
    }
}