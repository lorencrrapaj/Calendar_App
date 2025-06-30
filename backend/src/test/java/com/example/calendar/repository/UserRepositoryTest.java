package com.example.calendar.repository;

import com.example.calendar.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .build();
    }

    @Test
    void findByEmail_UserExists_ReturnsUser() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getPasswordHash()).isEqualTo("encodedPassword");
    }

    @Test
    void findByEmail_UserDoesNotExist_ReturnsEmpty() {
        // When
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_CaseInsensitive_ReturnsEmpty() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> result = userRepository.findByEmail("TEST@EXAMPLE.COM");

        // Then
        assertThat(result).isEmpty(); // Email should be case-sensitive
    }

    @Test
    void existsByEmail_UserExists_ReturnsTrue() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_UserDoesNotExist_ReturnsFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_CaseInsensitive_ReturnsFalse() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByEmail("TEST@EXAMPLE.COM");

        // Then
        assertThat(exists).isFalse(); // Email should be case-sensitive
    }

    @Test
    void save_NewUser_PersistsUser() {
        // Given
        User newUser = User.builder()
                .email("new@example.com")
                .passwordHash("newPassword")
                .build();

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("newPassword");

        // Verify it's actually persisted
        Optional<User> foundUser = userRepository.findByEmail("new@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void save_UpdateExistingUser_UpdatesUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Long userId = savedUser.getId();

        // When
        savedUser.setPasswordHash("newEncodedPassword");
        User updatedUser = userRepository.save(savedUser);
        entityManager.flush(); // Ensure changes are flushed to database

        // Then
        assertThat(updatedUser.getId()).isEqualTo(userId);
        assertThat(updatedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(updatedUser.getPasswordHash()).isEqualTo("newEncodedPassword");

        // Verify it's actually updated in the database
        entityManager.clear(); // Clear the persistence context
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getPasswordHash()).isEqualTo("newEncodedPassword");
    }

    @Test
    void findAll_MultipleUsers_ReturnsAllUsers() {
        // Given
        User user1 = User.builder()
                .email("user1@example.com")
                .passwordHash("password1")
                .build();
        User user2 = User.builder()
                .email("user2@example.com")
                .passwordHash("password2")
                .build();

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // When
        var users = userRepository.findAll();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    void deleteById_ExistingUser_RemovesUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Long userId = savedUser.getId();

        // When
        userRepository.deleteById(userId);

        // Then
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        assertThat(foundUser).isEmpty();
        assertThat(userRepository.existsByEmail("test@example.com")).isFalse();
    }
}
