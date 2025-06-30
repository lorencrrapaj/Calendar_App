package com.example.calendar.integration;

import com.example.calendar.dto.CreateEventDTO;
import com.example.calendar.dto.EventDTO;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class EventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create and save a test user
        testUser = User.builder()
                .email("integration@example.com")
                .passwordHash("hashedPassword")
                .build();
        userRepository.save(testUser);
    }

    @Test
    @WithMockUser(username = "integration@example.com")
    void createEventThenGetEvents_Success_PersistenceFlow() throws Exception {
        // Given - Create event DTO
        LocalDateTime startTime = LocalDateTime.of(2024, 2, 15, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 2, 15, 12, 0);

        CreateEventDTO createEventDTO = new CreateEventDTO();
        createEventDTO.setTitle("Integration Test Event");
        createEventDTO.setDescription("This is an integration test event");
        createEventDTO.setStartDateTime(startTime);
        createEventDTO.setEndDateTime(endTime);

        // When - Create the event
        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Event"))
                .andExpect(jsonPath("$.description").value("This is an integration test event"))
                .andExpect(jsonPath("$.userEmail").value("integration@example.com"))
                .andReturn();

        // Extract the created event ID
        String responseContent = createResult.getResponse().getContentAsString();
        EventDTO createdEvent = objectMapper.readValue(responseContent, EventDTO.class);
        assertThat(createdEvent.getId()).isNotNull();

        // Then - Retrieve events and verify persistence
        mockMvc.perform(get("/api/events")
                        .param("start", "2024-02-01T00:00:00")
                        .param("end", "2024-02-28T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(createdEvent.getId()))
                .andExpect(jsonPath("$[0].title").value("Integration Test Event"))
                .andExpect(jsonPath("$[0].description").value("This is an integration test event"))
                .andExpect(jsonPath("$[0].userEmail").value("integration@example.com"));
    }

    @Test
    @WithMockUser(username = "integration@example.com")
    void createEvent_InvalidDateOrdering_Returns400() throws Exception {
        // Given - Create event DTO with invalid date ordering
        LocalDateTime startTime = LocalDateTime.of(2024, 2, 15, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 2, 15, 10, 0); // Before start time

        CreateEventDTO createEventDTO = new CreateEventDTO();
        createEventDTO.setTitle("Invalid Event");
        createEventDTO.setDescription("This event has invalid date ordering");
        createEventDTO.setStartDateTime(startTime);
        createEventDTO.setEndDateTime(endTime);

        // When & Then - Attempt to create the event
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("End date and time must be after start date and time"));

        // Verify no events were created
        mockMvc.perform(get("/api/events")
                        .param("start", "2024-02-01T00:00:00")
                        .param("end", "2024-02-28T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "integration@example.com")
    void createMultipleEventsThenGetEvents_Success_MultipleEventsFlow() throws Exception {
        // Given - Create multiple events
        CreateEventDTO event1 = new CreateEventDTO();
        event1.setTitle("First Event");
        event1.setDescription("First event description");
        event1.setStartDateTime(LocalDateTime.of(2024, 3, 10, 9, 0));
        event1.setEndDateTime(LocalDateTime.of(2024, 3, 10, 11, 0));

        CreateEventDTO event2 = new CreateEventDTO();
        event2.setTitle("Second Event");
        event2.setDescription("Second event description");
        event2.setStartDateTime(LocalDateTime.of(2024, 3, 15, 14, 0));
        event2.setEndDateTime(LocalDateTime.of(2024, 3, 15, 16, 0));

        CreateEventDTO event3 = new CreateEventDTO();
        event3.setTitle("Third Event");
        event3.setDescription("Third event description");
        event3.setStartDateTime(LocalDateTime.of(2024, 3, 20, 10, 0));
        event3.setEndDateTime(LocalDateTime.of(2024, 3, 20, 12, 0));

        // When - Create all events
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event3)))
                .andExpect(status().isCreated());

        // Then - Retrieve all events and verify they are ordered by start time
        mockMvc.perform(get("/api/events")
                        .param("start", "2024-03-01T00:00:00")
                        .param("end", "2024-03-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("First Event"))
                .andExpect(jsonPath("$[1].title").value("Second Event"))
                .andExpect(jsonPath("$[2].title").value("Third Event"));
    }

    @Test
    @WithMockUser(username = "integration@example.com")
    void getEvents_EmptyRange_ReturnsEmptyArray() throws Exception {
        // Given - Create an event outside the query range
        CreateEventDTO event = new CreateEventDTO();
        event.setTitle("Out of Range Event");
        event.setDescription("This event is outside the query range");
        event.setStartDateTime(LocalDateTime.of(2024, 1, 15, 10, 0));
        event.setEndDateTime(LocalDateTime.of(2024, 1, 15, 12, 0));

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated());

        // When & Then - Query for events in a different range
        mockMvc.perform(get("/api/events")
                        .param("start", "2024-06-01T00:00:00")
                        .param("end", "2024-06-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createEvent_Unauthorized_Returns401() throws Exception {
        // Given - Create event DTO
        CreateEventDTO createEventDTO = new CreateEventDTO();
        createEventDTO.setTitle("Unauthorized Event");
        createEventDTO.setDescription("This should fail");
        createEventDTO.setStartDateTime(LocalDateTime.of(2024, 2, 15, 10, 0));
        createEventDTO.setEndDateTime(LocalDateTime.of(2024, 2, 15, 12, 0));

        // When & Then - Attempt to create event without authentication
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventDTO)))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for unauthenticated requests
    }

    @Test
    void getEvents_Unauthorized_Returns401() throws Exception {
        // When & Then - Attempt to get events without authentication
        mockMvc.perform(get("/api/events")
                        .param("start", "2024-02-01T00:00:00")
                        .param("end", "2024-02-28T23:59:59"))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for unauthenticated requests
    }

    @Test
    @WithMockUser(username = "integration@example.com")
    void updateEvent_Success_PersistenceFlow() throws Exception {
        // Given - Create an event first
        CreateEventDTO createEventDTO = new CreateEventDTO();
        createEventDTO.setTitle("Original Event");
        createEventDTO.setDescription("Original description");
        createEventDTO.setStartDateTime(LocalDateTime.of(2024, 4, 15, 10, 0));
        createEventDTO.setEndDateTime(LocalDateTime.of(2024, 4, 15, 12, 0));

        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        EventDTO createdEvent = objectMapper.readValue(responseContent, EventDTO.class);

        // When - Update the event
        CreateEventDTO updateEventDTO = new CreateEventDTO();
        updateEventDTO.setTitle("Updated Event");
        updateEventDTO.setDescription("Updated description");
        updateEventDTO.setStartDateTime(LocalDateTime.of(2024, 4, 15, 14, 0));
        updateEventDTO.setEndDateTime(LocalDateTime.of(2024, 4, 15, 16, 0));

        mockMvc.perform(put("/api/events/" + createdEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdEvent.getId()))
                .andExpect(jsonPath("$.title").value("Updated Event"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.userEmail").value("integration@example.com"));

        // Then - Verify the event was updated in the database
        mockMvc.perform(get("/api/events")
                        .param("start", "2024-04-01T00:00:00")
                        .param("end", "2024-04-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(createdEvent.getId()))
                .andExpect(jsonPath("$[0].title").value("Updated Event"))
                .andExpect(jsonPath("$[0].description").value("Updated description"));
    }

    @Test
    @WithMockUser(username = "integration@example.com")
    void updateEvent_NotFound_Returns404() throws Exception {
        // Given - Update DTO for non-existent event
        CreateEventDTO updateEventDTO = new CreateEventDTO();
        updateEventDTO.setTitle("Updated Event");
        updateEventDTO.setDescription("Updated description");
        updateEventDTO.setStartDateTime(LocalDateTime.of(2024, 4, 15, 14, 0));
        updateEventDTO.setEndDateTime(LocalDateTime.of(2024, 4, 15, 16, 0));

        // When & Then - Attempt to update non-existent event
        mockMvc.perform(put("/api/events/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event not found"));
    }

    @Test
    @WithMockUser(username = "integration@example.com")
    void deleteEvent_Success_PersistenceFlow() throws Exception {
        // Given - Create an event first
        CreateEventDTO createEventDTO = new CreateEventDTO();
        createEventDTO.setTitle("Event to Delete");
        createEventDTO.setDescription("This event will be deleted");
        createEventDTO.setStartDateTime(LocalDateTime.of(2024, 5, 15, 10, 0));
        createEventDTO.setEndDateTime(LocalDateTime.of(2024, 5, 15, 12, 0));

        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        EventDTO createdEvent = objectMapper.readValue(responseContent, EventDTO.class);

        // Verify event exists
        mockMvc.perform(get("/api/events")
                        .param("start", "2024-05-01T00:00:00")
                        .param("end", "2024-05-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // When - Delete the event
        mockMvc.perform(delete("/api/events/" + createdEvent.getId()))
                .andExpect(status().isNoContent());

        // Then - Verify the event was deleted from the database
        mockMvc.perform(get("/api/events")
                        .param("start", "2024-05-01T00:00:00")
                        .param("end", "2024-05-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "integration@example.com")
    void deleteEvent_NotFound_Returns404() throws Exception {
        // When & Then - Attempt to delete non-existent event
        mockMvc.perform(delete("/api/events/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event not found"));
    }

    @Test
    void updateEvent_Unauthorized_Returns403() throws Exception {
        // Given - Update DTO
        CreateEventDTO updateEventDTO = new CreateEventDTO();
        updateEventDTO.setTitle("Updated Event");
        updateEventDTO.setDescription("Updated description");
        updateEventDTO.setStartDateTime(LocalDateTime.of(2024, 4, 15, 14, 0));
        updateEventDTO.setEndDateTime(LocalDateTime.of(2024, 4, 15, 16, 0));

        // When & Then - Attempt to update without authentication
        mockMvc.perform(put("/api/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEvent_Unauthorized_Returns403() throws Exception {
        // When & Then - Attempt to delete without authentication
        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isForbidden());
    }
}
