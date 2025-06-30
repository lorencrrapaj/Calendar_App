package com.example.calendar.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoToStringTests {

    @Test
    void changePasswordDto_toString_includesAllFields() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldSecret123");
        dto.setNewPassword("newSecret456");

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("oldPassword=oldSecret123"));
        assertTrue(result.contains("newPassword=newSecret456"));
        assertTrue(result.contains("ChangePasswordDTO"));
    }

    @Test
    void createEventDto_toString_includesAllFields() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Test Event");
        dto.setDescription("Test Description");
        dto.setStartDateTime(LocalDateTime.of(2024, 1, 15, 10, 0));
        dto.setEndDateTime(LocalDateTime.of(2024, 1, 15, 12, 0));
        dto.setRecurrenceRule("FREQ=DAILY");
        dto.setRecurrenceEndDate(LocalDateTime.of(2024, 2, 15, 12, 0));
        dto.setRecurrenceCount(30);

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("title=Test Event"));
        assertTrue(result.contains("description=Test Description"));
        assertTrue(result.contains("startDateTime=2024-01-15T10:00"));
        assertTrue(result.contains("endDateTime=2024-01-15T12:00"));
        assertTrue(result.contains("recurrenceRule=FREQ=DAILY"));
        assertTrue(result.contains("recurrenceEndDate=2024-02-15T12:00"));
        assertTrue(result.contains("recurrenceCount=30"));
        assertTrue(result.contains("CreateEventDTO"));
    }

    @Test
    void eventDto_toString_includesAllFields() {
        // Given
        EventDTO dto = EventDTO.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .endDateTime(LocalDateTime.of(2024, 1, 15, 12, 0))
                .userEmail("test@example.com")
                .recurrenceRule("FREQ=WEEKLY")
                .recurrenceEndDate(LocalDateTime.of(2024, 3, 15, 12, 0))
                .recurrenceCount(12)
                .parentEventId(2L)
                .originalStartDateTime(LocalDateTime.of(2024, 1, 14, 10, 0))
                .excludedDates("2024-01-21T10:00,2024-01-28T10:00")
                .build();

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("title=Test Event"));
        assertTrue(result.contains("description=Test Description"));
        assertTrue(result.contains("startDateTime=2024-01-15T10:00"));
        assertTrue(result.contains("endDateTime=2024-01-15T12:00"));
        assertTrue(result.contains("userEmail=test@example.com"));
        assertTrue(result.contains("recurrenceRule=FREQ=WEEKLY"));
        assertTrue(result.contains("recurrenceEndDate=2024-03-15T12:00"));
        assertTrue(result.contains("recurrenceCount=12"));
        assertTrue(result.contains("parentEventId=2"));
        assertTrue(result.contains("originalStartDateTime=2024-01-14T10:00"));
        assertTrue(result.contains("excludedDates=2024-01-21T10:00,2024-01-28T10:00"));
        assertTrue(result.contains("EventDTO"));
    }

    @Test
    void loginDto_toString_includesAllFields() {
        // Given
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("secret123");

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("email=test@example.com"));
        assertTrue(result.contains("password=secret123"));
        assertTrue(result.contains("LoginDTO"));
    }

    @Test
    void loginResponse_toString_includesAllFields() {
        // Given
        LoginResponse dto = new LoginResponse();
        dto.setToken("jwt-token-12345");

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("token=jwt-token-12345"));
        assertTrue(result.contains("LoginResponse"));
    }

    @Test
    void registerDto_toString_includesAllFields() {
        // Given
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("register@example.com");
        dto.setPassword("password456");

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("email=register@example.com"));
        assertTrue(result.contains("password=password456"));
        assertTrue(result.contains("RegisterDTO"));
    }

    @Test
    void changePasswordDto_toString_withNullValues() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword(null);
        dto.setNewPassword(null);

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("oldPassword=null"));
        assertTrue(result.contains("newPassword=null"));
        assertTrue(result.contains("ChangePasswordDTO"));
    }

    @Test
    void createEventDto_toString_withNullOptionalFields() {
        // Given
        CreateEventDTO dto = new CreateEventDTO();
        dto.setTitle("Test Event");
        dto.setDescription("Test Description");
        dto.setStartDateTime(LocalDateTime.of(2024, 1, 15, 10, 0));
        dto.setEndDateTime(LocalDateTime.of(2024, 1, 15, 12, 0));
        dto.setRecurrenceRule(null);
        dto.setRecurrenceEndDate(null);
        dto.setRecurrenceCount(null);

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("title=Test Event"));
        assertTrue(result.contains("description=Test Description"));
        assertTrue(result.contains("startDateTime=2024-01-15T10:00"));
        assertTrue(result.contains("endDateTime=2024-01-15T12:00"));
        assertTrue(result.contains("recurrenceRule=null"));
        assertTrue(result.contains("recurrenceEndDate=null"));
        assertTrue(result.contains("recurrenceCount=null"));
        assertTrue(result.contains("CreateEventDTO"));
    }

    @Test
    void eventDto_toString_withNullOptionalFields() {
        // Given
        EventDTO dto = EventDTO.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .endDateTime(LocalDateTime.of(2024, 1, 15, 12, 0))
                .userEmail("test@example.com")
                .recurrenceRule(null)
                .recurrenceEndDate(null)
                .recurrenceCount(null)
                .parentEventId(null)
                .originalStartDateTime(null)
                .excludedDates(null)
                .build();

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("title=Test Event"));
        assertTrue(result.contains("description=Test Description"));
        assertTrue(result.contains("startDateTime=2024-01-15T10:00"));
        assertTrue(result.contains("endDateTime=2024-01-15T12:00"));
        assertTrue(result.contains("userEmail=test@example.com"));
        assertTrue(result.contains("recurrenceRule=null"));
        assertTrue(result.contains("recurrenceEndDate=null"));
        assertTrue(result.contains("recurrenceCount=null"));
        assertTrue(result.contains("parentEventId=null"));
        assertTrue(result.contains("originalStartDateTime=null"));
        assertTrue(result.contains("excludedDates=null"));
        assertTrue(result.contains("EventDTO"));
    }

    @Test
    void loginDto_toString_withEmptyValues() {
        // Given
        LoginDTO dto = new LoginDTO();
        dto.setEmail("");
        dto.setPassword("");

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("email="));
        assertTrue(result.contains("password="));
        assertTrue(result.contains("LoginDTO"));
    }

    @Test
    void loginResponse_toString_withNullToken() {
        // Given
        LoginResponse dto = new LoginResponse();
        dto.setToken(null);

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("token=null"));
        assertTrue(result.contains("LoginResponse"));
    }

    @Test
    void registerDto_toString_withSpecialCharacters() {
        // Given
        RegisterDTO dto = new RegisterDTO();
        dto.setEmail("test+special@example.com");
        dto.setPassword("p@ssw0rd!#$%");

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("email=test+special@example.com"));
        assertTrue(result.contains("password=p@ssw0rd!#$%"));
        assertTrue(result.contains("RegisterDTO"));
    }

    @Test
    void changePasswordDto_toString_withLongValues() {
        // Given
        ChangePasswordDTO dto = new ChangePasswordDTO();
        String longPassword = "this.is.a.very.long.password.that.might.cause.issues.in.toString.representation";
        dto.setOldPassword(longPassword);
        dto.setNewPassword(longPassword + ".new");

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("oldPassword=" + longPassword));
        assertTrue(result.contains("newPassword=" + longPassword + ".new"));
        assertTrue(result.contains("ChangePasswordDTO"));
    }

    @Test
    void eventDto_toString_withZeroValues() {
        // Given
        EventDTO dto = EventDTO.builder()
                .id(0L)
                .title("")
                .description("")
                .startDateTime(LocalDateTime.of(2024, 1, 1, 0, 0))
                .endDateTime(LocalDateTime.of(2024, 1, 1, 0, 0))
                .userEmail("")
                .recurrenceCount(0)
                .parentEventId(0L)
                .build();

        // When
        String result = dto.toString();

        // Then
        assertTrue(result.contains("id=0"));
        assertTrue(result.contains("title="));
        assertTrue(result.contains("description="));
        assertTrue(result.contains("startDateTime=2024-01-01T00:00"));
        assertTrue(result.contains("endDateTime=2024-01-01T00:00"));
        assertTrue(result.contains("userEmail="));
        assertTrue(result.contains("recurrenceCount=0"));
        assertTrue(result.contains("parentEventId=0"));
        assertTrue(result.contains("EventDTO"));
    }
}
