package com.example.calendar.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DTOTest {

    @Test
    void loginDTO_GettersAndSetters() {
        // Given
        LoginDTO dto = new LoginDTO();
        String email = "test@example.com";
        String password = "password123";

        // When
        dto.setEmail(email);
        dto.setPassword(password);

        // Then
        assertThat(dto.getEmail()).isEqualTo(email);
        assertThat(dto.getPassword()).isEqualTo(password);
    }

    @Test
    void loginDTO_EqualsAndHashCode() {
        // Given
        LoginDTO dto1 = new LoginDTO();
        dto1.setEmail("test@example.com");
        dto1.setPassword("password123");

        LoginDTO dto2 = new LoginDTO();
        dto2.setEmail("test@example.com");
        dto2.setPassword("password123");

        LoginDTO dto3 = new LoginDTO();
        dto3.setEmail("different@example.com");
        dto3.setPassword("password123");

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    void loginDTO_ToString() {
        // Given
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).contains("test@example.com");
        assertThat(toString).contains("password123");
    }

    @Test
    void registerDTO_GettersAndSetters() {
        // Given
        RegisterDTO dto = new RegisterDTO();
        String email = "register@example.com";
        String password = "registerpass123";

        // When
        dto.setEmail(email);
        dto.setPassword(password);

        // Then
        assertThat(dto.getEmail()).isEqualTo(email);
        assertThat(dto.getPassword()).isEqualTo(password);
    }

    @Test
    void registerDTO_EqualsAndHashCode() {
        // Given
        RegisterDTO dto1 = new RegisterDTO();
        dto1.setEmail("register@example.com");
        dto1.setPassword("password123");

        RegisterDTO dto2 = new RegisterDTO();
        dto2.setEmail("register@example.com");
        dto2.setPassword("password123");

        RegisterDTO dto3 = new RegisterDTO();
        dto3.setEmail("different@example.com");
        dto3.setPassword("password123");

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    void registerDTO_ToString() {
        // Given
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("register@example.com");
        dto.setPassword("password123");

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).contains("register@example.com");
        assertThat(toString).contains("password123");
    }

    @Test
    void changePasswordDTO_GettersAndSetters() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO();
        String oldPassword = "oldpass123";
        String newPassword = "newpass456";

        // When
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);

        // Then
        assertThat(dto.getOldPassword()).isEqualTo(oldPassword);
        assertThat(dto.getNewPassword()).isEqualTo(newPassword);
    }

    @Test
    void changePasswordDTO_EqualsAndHashCode() {
        // Given
        ChangePasswordDTO dto1 = new ChangePasswordDTO();
        dto1.setOldPassword("oldpass123");
        dto1.setNewPassword("newpass456");

        ChangePasswordDTO dto2 = new ChangePasswordDTO();
        dto2.setOldPassword("oldpass123");
        dto2.setNewPassword("newpass456");

        ChangePasswordDTO dto3 = new ChangePasswordDTO();
        dto3.setOldPassword("different123");
        dto3.setNewPassword("newpass456");

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    void changePasswordDTO_ToString() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldpass123");
        dto.setNewPassword("newpass456");

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).contains("oldpass123");
        assertThat(toString).contains("newpass456");
    }

    @Test
    void loginResponse_Constructor() {
        // Given
        String token = "jwt-token-123";

        // When
        LoginResponse response = new LoginResponse(token);

        // Then
        assertThat(response.getToken()).isEqualTo(token);
    }

    @Test
    void loginResponse_GettersAndSetters() {
        // Given
        LoginResponse response = new LoginResponse("initial-token");
        String newToken = "new-jwt-token-456";

        // When
        response.setToken(newToken);

        // Then
        assertThat(response.getToken()).isEqualTo(newToken);
    }

    @Test
    void loginResponse_EqualsAndHashCode() {
        // Given
        LoginResponse response1 = new LoginResponse("token123");
        LoginResponse response2 = new LoginResponse("token123");
        LoginResponse response3 = new LoginResponse("different-token");

        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1).isNotEqualTo(response3);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1.hashCode()).isNotEqualTo(response3.hashCode());
    }

    @Test
    void loginResponse_ToString() {
        // Given
        LoginResponse response = new LoginResponse("jwt-token-123");

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).contains("jwt-token-123");
    }

    @Test
    void loginDTO_NullValues() {
        // Given
        LoginDTO dto = new LoginDTO();

        // When
        dto.setEmail(null);
        dto.setPassword(null);

        // Then
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getPassword()).isNull();
    }

    @Test
    void registerDTO_NullValues() {
        // Given
        RegisterDTO dto = new RegisterDTO();

        // When
        dto.setEmail(null);
        dto.setPassword(null);

        // Then
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getPassword()).isNull();
    }

    @Test
    void changePasswordDTO_NullValues() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO();

        // When
        dto.setOldPassword(null);
        dto.setNewPassword(null);

        // Then
        assertThat(dto.getOldPassword()).isNull();
        assertThat(dto.getNewPassword()).isNull();
    }

    @Test
    void loginResponse_NullValue() {
        // Given & When
        LoginResponse response = new LoginResponse(null);

        // Then
        assertThat(response.getToken()).isNull();
    }
}