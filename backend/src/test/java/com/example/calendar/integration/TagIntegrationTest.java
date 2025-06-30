package com.example.calendar.integration;

import com.example.calendar.dto.CreateEventDTO;
import com.example.calendar.dto.CreateTagDTO;
import com.example.calendar.dto.EventDTO;
import com.example.calendar.dto.TagDTO;
import com.example.calendar.model.User;
import com.example.calendar.repository.UserRepository;
import com.example.calendar.service.EventService;
import com.example.calendar.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public class TagIntegrationTest {

    @Autowired
    private TagService tagService;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private TagDTO workTag;
    private TagDTO personalTag;

    @BeforeEach
    void setUp() {
        // Create test user with unique email to avoid constraint violations
        String uniqueEmail = "test-" + System.currentTimeMillis() + "@example.com";
        testUser = User.builder()
                .email(uniqueEmail)
                .passwordHash("password")
                .build();
        testUser = userRepository.save(testUser);

        // Create test tags
        CreateTagDTO workTagDTO = new CreateTagDTO();
        workTagDTO.setName("Work");
        workTag = tagService.createTag(workTagDTO, testUser.getEmail());

        CreateTagDTO personalTagDTO = new CreateTagDTO();
        personalTagDTO.setName("Personal");
        personalTag = tagService.createTag(personalTagDTO, testUser.getEmail());
    }

    @Test
    void testCompleteTagWorkflow() {
        // 1. Verify tags were created
        List<TagDTO> allTags = tagService.getAllTags(testUser.getEmail());
        assertEquals(2, allTags.size());
        assertTrue(allTags.stream().anyMatch(tag -> "Work".equals(tag.getName())));
        assertTrue(allTags.stream().anyMatch(tag -> "Personal".equals(tag.getName())));

        // 2. Create events with tags
        CreateEventDTO workEventDTO = new CreateEventDTO();
        workEventDTO.setTitle("Work Meeting");
        workEventDTO.setDescription("Important meeting");
        workEventDTO.setStartDateTime(LocalDateTime.now().plusDays(1));
        workEventDTO.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(1));
        workEventDTO.setTagIds(List.of(workTag.getId()));

        EventDTO workEvent = eventService.createEvent(workEventDTO, testUser.getEmail());
        assertNotNull(workEvent);
        assertEquals(1, workEvent.getTags().size());
        assertEquals("Work", workEvent.getTags().get(0).getName());

        CreateEventDTO personalEventDTO = new CreateEventDTO();
        personalEventDTO.setTitle("Personal Task");
        personalEventDTO.setDescription("Personal task");
        personalEventDTO.setStartDateTime(LocalDateTime.now().plusDays(2));
        personalEventDTO.setEndDateTime(LocalDateTime.now().plusDays(2).plusHours(1));
        personalEventDTO.setTagIds(List.of(personalTag.getId()));

        EventDTO personalEvent = eventService.createEvent(personalEventDTO, testUser.getEmail());
        assertNotNull(personalEvent);
        assertEquals(1, personalEvent.getTags().size());
        assertEquals("Personal", personalEvent.getTags().get(0).getName());

        // 3. Test filtering by tag
        LocalDateTime rangeStart = LocalDateTime.now();
        LocalDateTime rangeEnd = LocalDateTime.now().plusDays(7);

        // Get all events
        List<EventDTO> allEvents = eventService.getEventsForUserInRange(
                testUser.getEmail(), rangeStart, rangeEnd);
        assertEquals(2, allEvents.size());

        // Filter by work tag
        List<EventDTO> workEvents = eventService.getEventsForUserInRange(
                testUser.getEmail(), rangeStart, rangeEnd, workTag.getId());
        assertEquals(1, workEvents.size());
        assertEquals("Work Meeting", workEvents.get(0).getTitle());

        // Filter by personal tag
        List<EventDTO> personalEvents = eventService.getEventsForUserInRange(
                testUser.getEmail(), rangeStart, rangeEnd, personalTag.getId());
        assertEquals(1, personalEvents.size());
        assertEquals("Personal Task", personalEvents.get(0).getTitle());

        // 4. Test tag deletion - create a tag without events and delete it
        CreateTagDTO tempTagDTO = new CreateTagDTO();
        tempTagDTO.setName("Temporary");
        TagDTO tempTag = tagService.createTag(tempTagDTO, testUser.getEmail());

        List<TagDTO> tagsBeforeDelete = tagService.getAllTags(testUser.getEmail());
        assertEquals(3, tagsBeforeDelete.size()); // Work, Personal, Temporary

        tagService.deleteTag(tempTag.getId(), testUser.getEmail());
        List<TagDTO> remainingTags = tagService.getAllTags(testUser.getEmail());
        assertEquals(2, remainingTags.size()); // Work, Personal
        assertTrue(remainingTags.stream().anyMatch(tag -> "Work".equals(tag.getName())));
        assertTrue(remainingTags.stream().anyMatch(tag -> "Personal".equals(tag.getName())));
        assertFalse(remainingTags.stream().anyMatch(tag -> "Temporary".equals(tag.getName())));
    }

    @Test
    void testEventWithMultipleTags() {
        // Create event with multiple tags
        CreateEventDTO eventDTO = new CreateEventDTO();
        eventDTO.setTitle("Mixed Event");
        eventDTO.setDescription("Event with multiple tags");
        eventDTO.setStartDateTime(LocalDateTime.now().plusDays(1));
        eventDTO.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(1));
        eventDTO.setTagIds(List.of(workTag.getId(), personalTag.getId()));

        EventDTO event = eventService.createEvent(eventDTO, testUser.getEmail());
        assertNotNull(event);
        assertEquals(2, event.getTags().size());

        // Verify both tags are present
        List<String> tagNames = event.getTags().stream()
                .map(TagDTO::getName)
                .toList();
        assertTrue(tagNames.contains("Work"));
        assertTrue(tagNames.contains("Personal"));
    }
}
