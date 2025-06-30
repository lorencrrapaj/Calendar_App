package com.example.calendar.controller;

import com.example.calendar.dto.ChangePasswordDTO;
import com.example.calendar.dto.LoginDTO;
import com.example.calendar.dto.LoginResponse;
import com.example.calendar.dto.RegisterDTO;
import com.example.calendar.model.User;
import com.example.calendar.repository.UserRepository;
import com.example.calendar.security.TokenStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenStore tokenStore;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_Success() throws Exception {
        // Given
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("test@example.com");
        registerDTO.setPassword("Password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered"));

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists() throws Exception {
        // Given
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("test@example.com");
        registerDTO.setPassword("Password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already in use"));

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() throws Exception {
        // Given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("Password123");

        User user = User.builder()
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "encodedPassword")).thenReturn(true);
        when(tokenStore.create("test@example.com")).thenReturn("jwt-token");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("Password123", "encodedPassword");
        verify(tokenStore).create("test@example.com");
    }

    @Test
    void login_UserNotFound() throws Exception {
        // Given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("Password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenStore, never()).create(anyString());
    }

    @Test
    void login_InvalidPassword() throws Exception {
        // Given
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("WrongPassword");

        User user = User.builder()
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("WrongPassword", "encodedPassword");
        verify(tokenStore, never()).create(anyString());
    }

    @Test
    void logout_Success() throws Exception {
        // Given
        String authHeader = "Bearer jwt-token";

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out"));

        verify(tokenStore).invalidate("jwt-token");
    }

    @Test
    void logout_NoAuthHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out"));

        verify(tokenStore, never()).invalidate(anyString());
    }

    @Test
    void logout_InvalidAuthHeader() throws Exception {
        // Given
        String authHeader = "InvalidHeader";

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out"));

        verify(tokenStore, never()).invalidate(anyString());
    }

    @Test
    void me_Success() throws Exception {
        // Given
        Principal principal = () -> "test@example.com";

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void me_Unauthorized_NoPrincipal() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_Unauthorized_NoPrincipal() throws Exception {
        // Given
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword("OldPassword123");
        changePasswordDTO.setNewPassword("NewPassword123");

        // When & Then
        mockMvc.perform(put("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not authenticated"));

        verify(userRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_Success() throws Exception {
        // Given
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword("OldPassword123");
        changePasswordDTO.setNewPassword("NewPassword123");

        Principal principal = () -> "test@example.com";

        User user = User.builder()
                .email("test@example.com")
                .passwordHash("oldEncodedPassword")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword123", "oldEncodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When & Then
        mockMvc.perform(put("/api/auth/password")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed"));

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("OldPassword123", "oldEncodedPassword");
        verify(passwordEncoder).encode("NewPassword123");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_UserNotFound() throws Exception {
        // Given
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword("OldPassword123");
        changePasswordDTO.setNewPassword("NewPassword123");

        Principal principal = () -> "test@example.com";

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/auth/password")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not found"));

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_InvalidOldPassword() throws Exception {
        // Given
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword("WrongOldPassword");
        changePasswordDTO.setNewPassword("NewPassword123");

        Principal principal = () -> "test@example.com";

        User user = User.builder()
                .email("test@example.com")
                .passwordHash("oldEncodedPassword")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongOldPassword", "oldEncodedPassword")).thenReturn(false);

        // When & Then
        mockMvc.perform(put("/api/auth/password")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid current password"));

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("WrongOldPassword", "oldEncodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
