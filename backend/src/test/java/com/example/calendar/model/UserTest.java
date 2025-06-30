package com.example.calendar.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void builder_CreatesUserWithAllFields() {
        // Given
        Long id = 1L;
        String email = "test@example.com";
        String passwordHash = "hashedPassword";

        // When
        User user = User.builder()
                .id(id)
                .email(email)
                .passwordHash(passwordHash)
                .build();

        // Then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
    }

    @Test
    void builder_CreatesUserWithPartialFields() {
        // Given
        String email = "test@example.com";
        String passwordHash = "hashedPassword";

        // When
        User user = User.builder()
                .email(email)
                .passwordHash(passwordHash)
                .build();

        // Then
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
    }

    @Test
    void noArgsConstructor_CreatesEmptyUser() {
        // When
        User user = new User();

        // Then
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPasswordHash()).isNull();
    }

    @Test
    void allArgsConstructor_CreatesUserWithAllFields() {
        // Given
        Long id = 1L;
        String email = "test@example.com";
        String passwordHash = "hashedPassword";

        // When
        User user = new User(id, email, passwordHash);

        // Then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
    }

    @Test
    void setters_UpdateUserFields() {
        // Given
        User user = new User();
        Long id = 1L;
        String email = "test@example.com";
        String passwordHash = "hashedPassword";

        // When
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);

        // Then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
    }

    @Test
    void getters_ReturnCorrectValues() {
        // Given
        Long id = 1L;
        String email = "test@example.com";
        String passwordHash = "hashedPassword";
        User user = new User(id, email, passwordHash);

        // When & Then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
    }

    @Test
    void builder_HandlesNullValues() {
        // When
        User user = User.builder()
                .id(null)
                .email(null)
                .passwordHash(null)
                .build();

        // Then
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPasswordHash()).isNull();
    }

    @Test
    void setters_HandleNullValues() {
        // Given
        User user = new User(1L, "test@example.com", "password");

        // When
        user.setId(null);
        user.setEmail(null);
        user.setPasswordHash(null);

        // Then
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPasswordHash()).isNull();
    }

    @Test
    void builder_HandlesEmptyStrings() {
        // When
        User user = User.builder()
                .email("")
                .passwordHash("")
                .build();

        // Then
        assertThat(user.getEmail()).isEqualTo("");
        assertThat(user.getPasswordHash()).isEqualTo("");
    }

    @Test
    void setters_HandleEmptyStrings() {
        // Given
        User user = new User();

        // When
        user.setEmail("");
        user.setPasswordHash("");

        // Then
        assertThat(user.getEmail()).isEqualTo("");
        assertThat(user.getPasswordHash()).isEqualTo("");
    }

    @Test
    void toString_ReturnsNonNullString() {
        // Given
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .build();

        // When
        String toString = user.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).isNotEmpty();
    }

    @Test
    void toString_HandlesNullFields() {
        // Given
        User user = new User();

        // When
        String toString = user.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).isNotEmpty();
    }

    @Test
    void objectIdentity_SameReference_ReturnsTrue() {
        // Given
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("password")
                .build();

        // When & Then - Test object identity (not equality since User doesn't override equals)
        assertThat(user).isEqualTo(user); // Same reference
        assertThat(user).isNotEqualTo(null);
        assertThat(user).isNotEqualTo("not a user");
    }

    @Test
    void objectIdentity_DifferentObjects_ReturnsFalse() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("password")
                .build();

        User user2 = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("password")
                .build();

        // When & Then - Different objects should not be equal
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    void hashCode_ConsistentAcrossMultipleCalls() {
        // Given
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("password")
                .build();

        // When
        int hashCode1 = user.hashCode();
        int hashCode2 = user.hashCode();

        // Then
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void builder_ToString_ContainsBuilderInfo() {
        // When
        String builderToString = User.builder().toString();

        // Then
        assertThat(builderToString).contains("User.UserBuilder");
    }

    @Test
    void setters_ChainedCalls_WorkCorrectly() {
        // Given
        User user = new User();

        // When
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("password");

        // Then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("password");
    }

    @Test
    void builder_ChainedCalls_WorkCorrectly() {
        // When
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .id(2L) // Override previous id
                .passwordHash("password")
                .build();

        // Then
        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("password");
    }
}
