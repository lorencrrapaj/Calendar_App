package com.example.calendar.controller;

import com.example.calendar.dto.CreateEventDTO;
import com.example.calendar.dto.EventDTO;
import com.example.calendar.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.mock;

@WebMvcTest(controllers = EventController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateEventDTO validCreateEventDTO;
    private CreateEventDTO invalidCreateEventDTO;
    private EventDTO eventDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 12, 0);
        LocalDateTime invalidEndTime = LocalDateTime.of(2024, 1, 15, 9, 0); // Before start time

        validCreateEventDTO = new CreateEventDTO();
        validCreateEventDTO.setTitle("Test Event");
        validCreateEventDTO.setDescription("Test Description");
        validCreateEventDTO.setStartDateTime(startTime);
        validCreateEventDTO.setEndDateTime(endTime);

        invalidCreateEventDTO = new CreateEventDTO();
        invalidCreateEventDTO.setTitle("Invalid Event");
        invalidCreateEventDTO.setDescription("Invalid Description");
        invalidCreateEventDTO.setStartDateTime(startTime);
        invalidCreateEventDTO.setEndDateTime(invalidEndTime);

        eventDTO = EventDTO.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .startDateTime(startTime)
                .endDateTime(endTime)
                .userEmail("test@example.com")
                .build();
    }

    @Test
    void createEvent_Success_ValidDTO_Returns201() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(eventService.createEvent(any(CreateEventDTO.class), eq("test@example.com")))
                .thenReturn(eventDTO);

        // When & Then
        mockMvc.perform(post("/api/events")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateEventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"));
    }

    @Test
    void createEvent_Failure_EndDateBeforeStartDate_Returns400() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(eventService.createEvent(any(CreateEventDTO.class), eq("test@example.com")))
                .thenThrow(new IllegalArgumentException("End date and time must be after start date and time"));

        // When & Then
        mockMvc.perform(post("/api/events")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCreateEventDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("End date and time must be after start date and time"));
    }

    @Test
    void createEvent_Unauthorized_NoPrincipal_Returns401() throws Exception {
        // When & Then (Principal is null, so controller returns 401)
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateEventDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not authenticated"));
    }

    @Test
    void getEventsInRange_Success_NoEvents_ReturnsEmptyList() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(eventService.getEventsForUserInRange(eq("test@example.com"), any(LocalDateTime.class), any(LocalDateTime.class), any()))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/events")
                        .principal(mockPrincipal)
                        .param("start", "2024-01-01T00:00:00")
                        .param("end", "2024-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getEventsInRange_Success_OneEvent_ReturnsOneEvent() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        List<EventDTO> events = Collections.singletonList(eventDTO);

        when(eventService.getEventsForUserInRange(eq("test@example.com"), any(LocalDateTime.class), any(LocalDateTime.class), any()))
                .thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events")
                        .principal(mockPrincipal)
                        .param("start", "2024-01-01T00:00:00")
                        .param("end", "2024-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Test Event"));
    }

    @Test
    void getEventsInRange_Success_ManyEvents_ReturnsAllEvents() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        EventDTO event2 = EventDTO.builder()
                .id(2L)
                .title("Second Event")
                .description("Second Description")
                .startDateTime(LocalDateTime.of(2024, 1, 16, 14, 0))
                .endDateTime(LocalDateTime.of(2024, 1, 16, 16, 0))
                .userEmail("test@example.com")
                .build();

        EventDTO event3 = EventDTO.builder()
                .id(3L)
                .title("Third Event")
                .description("Third Description")
                .startDateTime(LocalDateTime.of(2024, 1, 17, 9, 0))
                .endDateTime(LocalDateTime.of(2024, 1, 17, 11, 0))
                .userEmail("test@example.com")
                .build();

        List<EventDTO> events = Arrays.asList(eventDTO, event2, event3);

        when(eventService.getEventsForUserInRange(eq("test@example.com"), any(LocalDateTime.class), any(LocalDateTime.class), any()))
                .thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events")
                        .principal(mockPrincipal)
                        .param("start", "2024-01-01T00:00:00")
                        .param("end", "2024-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("Test Event"))
                .andExpect(jsonPath("$[1].title").value("Second Event"))
                .andExpect(jsonPath("$[2].title").value("Third Event"));
    }

    @Test
    void getEventsInRange_Unauthorized_NoPrincipal_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/events")
                        .param("start", "2024-01-01T00:00:00")
                        .param("end", "2024-01-31T23:59:59"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not authenticated"));
    }

    @Test
    void createEvent_InternalServerError_GenericException_Returns500() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(eventService.createEvent(any(CreateEventDTO.class), eq("test@example.com")))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/events")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateEventDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while creating the event"));
    }

    @Test
    void getEventsInRange_BadRequest_IllegalArgumentException_Returns400() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(eventService.getEventsForUserInRange(eq("test@example.com"), any(LocalDateTime.class), any(LocalDateTime.class), any()))
                .thenThrow(new IllegalArgumentException("Invalid date range"));

        // When & Then
        mockMvc.perform(get("/api/events")
                        .principal(mockPrincipal)
                        .param("start", "2024-01-01T00:00:00")
                        .param("end", "2024-01-31T23:59:59"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid date range"));
    }

    @Test
    void getEventsInRange_InternalServerError_GenericException_Returns500() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(eventService.getEventsForUserInRange(eq("test@example.com"), any(LocalDateTime.class), any(LocalDateTime.class), any()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/events")
                        .principal(mockPrincipal)
                        .param("start", "2024-01-01T00:00:00")
                        .param("end", "2024-01-31T23:59:59"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while fetching events"));
    }

    // PUT /api/events/{id} tests
    @Test
    void updateEvent_Success_ValidDTO_ReturnsUpdatedEvent() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");

        EventDTO updatedEventDTO = EventDTO.builder()
                .id(1L)
                .title("Updated Event")
                .description("Updated Description")
                .startDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .endDateTime(LocalDateTime.of(2024, 1, 15, 12, 0))
                .userEmail("test@example.com")
                .build();

        when(eventService.updateEvent(eq(1L), any(CreateEventDTO.class), eq("test@example.com"), eq("single")))
                .thenReturn(updatedEventDTO);

        // When & Then
        mockMvc.perform(put("/api/events/1")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateEventDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Event"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    void updateEvent_Unauthorized_NoPrincipal_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateEventDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not authenticated"));
    }

    @Test
    void updateEvent_NotFound_InvalidID_Returns404() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(eventService.updateEvent(eq(999L), any(CreateEventDTO.class), eq("test@example.com"), eq("single")))
                .thenThrow(new IllegalArgumentException("Event not found"));

        // When & Then
        mockMvc.perform(put("/api/events/999")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateEventDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event not found"));
    }

    @Test
    void updateEvent_Forbidden_AccessDenied_Returns403() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(eventService.updateEvent(eq(1L), any(CreateEventDTO.class), eq("test@example.com"), eq("single")))
                .thenThrow(new IllegalArgumentException("Access denied: You can only update your own events"));

        // When & Then
        mockMvc.perform(put("/api/events/1")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateEventDTO)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access denied: You can only update your own events"));
    }

    @Test
    void updateEvent_BadRequest_ValidationError_Returns400() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(eventService.updateEvent(eq(1L), any(CreateEventDTO.class), eq("test@example.com"), eq("single")))
                .thenThrow(new IllegalArgumentException("End date and time must be after start date and time"));

        // When & Then
        mockMvc.perform(put("/api/events/1")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateEventDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("End date and time must be after start date and time"));
    }

    @Test
    void updateEvent_InternalServerError_GenericException_Returns500() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        when(eventService.updateEvent(eq(1L), any(CreateEventDTO.class), eq("test@example.com"), eq("single")))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(put("/api/events/1")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateEventDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while updating the event"));
    }

    // DELETE /api/events/{id} tests
    @Test
    void deleteEvent_Success_ValidID_Returns204() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");

        // When & Then
        mockMvc.perform(delete("/api/events/1")
                        .principal(mockPrincipal))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEvent_Unauthorized_NoPrincipal_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not authenticated"));
    }

    @Test
    void deleteEvent_NotFound_InvalidID_Returns404() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        doThrow(new IllegalArgumentException("Event not found"))
                .when(eventService).deleteEvent(eq(999L), eq("test@example.com"), eq("instance"));

        // When & Then
        mockMvc.perform(delete("/api/events/999")
                        .principal(mockPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event not found"));
    }

    @Test
    void deleteEvent_Forbidden_AccessDenied_Returns403() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        doThrow(new IllegalArgumentException("Access denied: You can only delete your own events"))
                .when(eventService).deleteEvent(eq(1L), eq("test@example.com"), eq("instance"));

        // When & Then
        mockMvc.perform(delete("/api/events/1")
                        .principal(mockPrincipal))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access denied: You can only delete your own events"));
    }

    @Test
    void deleteEvent_BadRequest_ValidationError_Returns400() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        doThrow(new IllegalArgumentException("Invalid scope parameter"))
                .when(eventService).deleteEvent(eq(1L), eq("test@example.com"), eq("instance"));

        // When & Then
        mockMvc.perform(delete("/api/events/1")
                        .principal(mockPrincipal))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid scope parameter"));
    }

    @Test
    void deleteEvent_InternalServerError_GenericException_Returns500() throws Exception {
        // Given
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("test@example.com");
        doThrow(new RuntimeException("Database connection failed"))
                .when(eventService).deleteEvent(eq(1L), eq("test@example.com"), eq("instance"));

        // When & Then
        mockMvc.perform(delete("/api/events/1")
                        .principal(mockPrincipal))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred while deleting the event"));
    }
}
