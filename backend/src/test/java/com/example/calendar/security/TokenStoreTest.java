package com.example.calendar.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenStoreTest {

    private TokenStore tokenStore;

    @BeforeEach
    void setUp() {
        tokenStore = new TokenStore();
    }

    @Test
    void create_GeneratesUniqueToken() {
        // Given
        String email = "test@example.com";

        // When
        String token1 = tokenStore.create(email);
        String token2 = tokenStore.create(email);

        // Then
        assertThat(token1).isNotNull();
        assertThat(token2).isNotNull();
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void create_StoresTokenEmailMapping() {
        // Given
        String email = "test@example.com";

        // When
        String token = tokenStore.create(email);

        // Then
        assertThat(tokenStore.getEmail(token)).isEqualTo(email);
    }

    @Test
    void getEmail_ValidToken_ReturnsEmail() {
        // Given
        String email = "test@example.com";
        String token = tokenStore.create(email);

        // When
        String result = tokenStore.getEmail(token);

        // Then
        assertThat(result).isEqualTo(email);
    }

    @Test
    void getEmail_InvalidToken_ReturnsNull() {
        // When
        String result = tokenStore.getEmail("invalid-token");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getEmail_NullToken_ReturnsNull() {
        // When
        String result = tokenStore.getEmail(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void invalidate_ValidToken_RemovesToken() {
        // Given
        String email = "test@example.com";
        String token = tokenStore.create(email);

        // When
        tokenStore.invalidate(token);

        // Then
        assertThat(tokenStore.getEmail(token)).isNull();
    }

    @Test
    void invalidate_InvalidToken_DoesNotThrowException() {
        // When & Then - should not throw exception
        tokenStore.invalidate("invalid-token");
    }

    @Test
    void invalidate_NullToken_DoesNotThrowException() {
        // When & Then - should not throw exception
        tokenStore.invalidate(null);
    }

    @Test
    void multipleTokensForSameEmail_BothWork() {
        // Given
        String email = "test@example.com";

        // When
        String token1 = tokenStore.create(email);
        String token2 = tokenStore.create(email);

        // Then
        assertThat(tokenStore.getEmail(token1)).isEqualTo(email);
        assertThat(tokenStore.getEmail(token2)).isEqualTo(email);
    }

    @Test
    void multipleTokensForDifferentEmails_BothWork() {
        // Given
        String email1 = "test1@example.com";
        String email2 = "test2@example.com";

        // When
        String token1 = tokenStore.create(email1);
        String token2 = tokenStore.create(email2);

        // Then
        assertThat(tokenStore.getEmail(token1)).isEqualTo(email1);
        assertThat(tokenStore.getEmail(token2)).isEqualTo(email2);
    }

    @Test
    void invalidateOneToken_DoesNotAffectOthers() {
        // Given
        String email1 = "test1@example.com";
        String email2 = "test2@example.com";
        String token1 = tokenStore.create(email1);
        String token2 = tokenStore.create(email2);

        // When
        tokenStore.invalidate(token1);

        // Then
        assertThat(tokenStore.getEmail(token1)).isNull();
        assertThat(tokenStore.getEmail(token2)).isEqualTo(email2);
    }
}