package com.example.calendar.integration;

import com.example.calendar.dto.ChangePasswordDTO;
import com.example.calendar.dto.LoginDTO;
import com.example.calendar.dto.RegisterDTO;
import com.example.calendar.model.User;
import com.example.calendar.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void fullAuthenticationFlow_Success() throws Exception {
        // 1. Register a new user
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("integration@example.com");
        registerDTO.setPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered"));

        // Verify user was created in database
        assertThat(userRepository.existsByEmail("integration@example.com")).isTrue();

        // 2. Login with the registered user
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("integration@example.com");
        loginDTO.setPassword("Password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Extract token from response
        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        // 3. Access protected endpoint /me
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@example.com"));

        // 4. Change password
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword("Password123");
        changePasswordDTO.setNewPassword("NewPassword456");

        mockMvc.perform(put("/api/auth/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed"));

        // 5. Verify old password no longer works
        LoginDTO oldPasswordLogin = new LoginDTO();
        oldPasswordLogin.setEmail("integration@example.com");
        oldPasswordLogin.setPassword("Password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldPasswordLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        // 6. Verify new password works
        LoginDTO newPasswordLogin = new LoginDTO();
        newPasswordLogin.setEmail("integration@example.com");
        newPasswordLogin.setPassword("NewPassword456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPasswordLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // 7. Logout
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out"));
    }

    @Test
    void register_DuplicateEmail_ReturnsConflict() throws Exception {
        // Given - create a user first
        User existingUser = User.builder()
                .email("existing@example.com")
                .passwordHash(passwordEncoder.encode("Password123"))
                .build();
        userRepository.save(existingUser);

        // When - try to register with same email
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("existing@example.com");
        registerDTO.setPassword("DifferentPassword456");

        // Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already in use"));
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Given - create a user
        User user = User.builder()
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("CorrectPassword"))
                .build();
        userRepository.save(user);

        // When - try to login with wrong password
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("WrongPassword");

        // Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void accessProtectedEndpoint_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_WithInvalidToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_WithWrongOldPassword_ReturnsUnauthorized() throws Exception {
        // Given - create user and get token
        User user = User.builder()
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("CorrectPassword"))
                .build();
        userRepository.save(user);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("CorrectPassword");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        // When - try to change password with wrong old password
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword("WrongOldPassword");
        changePasswordDTO.setNewPassword("NewPassword456");

        // Then
        mockMvc.perform(put("/api/auth/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid current password"));
    }

    @Test
    void databaseConstraints_UniqueEmailViolation_HandledCorrectly() throws Exception {
        // Given - create a user
        RegisterDTO registerDTO1 = new RegisterDTO();
        registerDTO1.setEmail("unique@example.com");
        registerDTO1.setPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO1)))
                .andExpect(status().isOk());

        // When - try to register another user with same email
        RegisterDTO registerDTO2 = new RegisterDTO();
        registerDTO2.setEmail("unique@example.com");
        registerDTO2.setPassword("DifferentPassword456");

        // Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO2)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already in use"));

        // Verify only one user exists
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void logout_WithValidToken_Success() throws Exception {
        // Given - create user and get token
        User user = User.builder()
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("Password123"))
                .build();
        userRepository.save(user);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("Password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        // When - logout with valid token
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Then - token should be invalidated
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithNonexistentUser_ReturnsUnauthorized() throws Exception {
        // Given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("nonexistent@example.com");
        loginDTO.setPassword("Password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }
}
