package com.example.calendar.service;

import com.example.calendar.dto.CreateEventDTO;
import com.example.calendar.dto.EventDTO;
import com.example.calendar.dto.TagDTO;
import com.example.calendar.model.Event;
import com.example.calendar.model.Tag;
import com.example.calendar.model.User;
import com.example.calendar.repository.EventRepository;
import com.example.calendar.repository.TagRepository;
import com.example.calendar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    @Transactional
    public EventDTO createEvent(CreateEventDTO dto, String userEmail) {
        // Validate that end time is after start time
        if (!dto.getEndDateTime().isAfter(dto.getStartDateTime())) {
            throw new IllegalArgumentException("End date and time must be after start date and time");
        }

        // Find the user
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        // Create and save the event
        Event event = Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startDateTime(dto.getStartDateTime())
                .endDateTime(dto.getEndDateTime())
                .user(user)
                .recurrenceRule(dto.getRecurrenceRule())
                .recurrenceEndDate(dto.getRecurrenceEndDate())
                .recurrenceCount(dto.getRecurrenceCount())
                .build();

        // Handle tag assignment
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(dto.getTagIds());
            if (tags.size() != dto.getTagIds().size()) {
                throw new IllegalArgumentException("One or more tags not found");
            }
            event.setTags(new HashSet<>(tags));
        }

        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }

    public List<EventDTO> getEventsForUserInRange(String userEmail, LocalDateTime start, LocalDateTime end) {
        return getEventsForUserInRange(userEmail, start, end, null);
    }

    public List<EventDTO> getEventsForUserInRange(String userEmail, LocalDateTime start, LocalDateTime end, Long tagId) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        List<Event> allEvents;
        if (tagId != null) {
            // Filter by tag using the repository query
            allEvents = eventRepository.findByUserAndStartDateTimeBetweenAndTagIdOrderByStartDateTimeAsc(user, start, end, tagId);
        } else {
            // Get all events for the user (we need to check recurring events that might start before the range)
            allEvents = eventRepository.findByUserOrderByStartDateTimeAsc(user);
        }

        List<EventDTO> result = new ArrayList<>();

        for (Event event : allEvents) {
            if (event.getParentEvent() != null) {
                // This is an instance override, include it if it falls within the range
                if (event.getStartDateTime().isBefore(end) && event.getEndDateTime().isAfter(start)) {
                    result.add(convertToDTO(event));
                }
            } else if (event.getRecurrenceRule() != null && !event.getRecurrenceRule().isEmpty()) {
                // This is a recurring event - expand it
                List<EventDTO> occurrences = expandRecurringEvent(event, start, end);
                result.addAll(occurrences);
            } else {
                // Regular event - include if it falls within the range
                if (event.getStartDateTime().isBefore(end) && event.getEndDateTime().isAfter(start)) {
                    result.add(convertToDTO(event));
                }
            }
        }

        return result.stream()
                .sorted((a, b) -> a.getStartDateTime().compareTo(b.getStartDateTime()))
                .collect(Collectors.toList());
    }

    public List<EventDTO> getUserEvents(String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        List<Event> events = eventRepository.findByUserOrderByStartDateTimeAsc(userOpt.get());
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventDTO updateEvent(Long id, CreateEventDTO dto, String userEmail) {
        return updateEvent(id, dto, userEmail, "single");
    }

    @Transactional
    public EventDTO updateEvent(Long id, CreateEventDTO dto, String userEmail, String scope) {
        // Validate that end time is after start time
        if (!dto.getEndDateTime().isAfter(dto.getStartDateTime())) {
            throw new IllegalArgumentException("End date and time must be after start date and time");
        }

        // Find the user
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        // Determine if this is a master event ID or an occurrence ID
        OccurrenceInfo occurrenceInfo = parseOccurrenceId(id);
        Event masterEvent;
        LocalDateTime occurrenceDateTime = null;

        if (occurrenceInfo != null) {
            // This is an occurrence ID, find the master event
            Optional<Event> eventOpt = eventRepository.findById(occurrenceInfo.masterEventId);
            if (eventOpt.isEmpty()) {
                throw new IllegalArgumentException("Master event not found");
            }
            masterEvent = eventOpt.get();
            occurrenceDateTime = occurrenceInfo.occurrenceDateTime;
        } else {
            // This is a master event ID
            Optional<Event> eventOpt = eventRepository.findById(id);
            if (eventOpt.isEmpty()) {
                throw new IllegalArgumentException("Event not found");
            }
            masterEvent = eventOpt.get();
        }

        // Verify ownership
        if (!masterEvent.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied: You can only update your own events");
        }

        // Handle different scopes for recurring events
        if ("instance".equals(scope) && occurrenceDateTime != null) {
            // Update only this specific occurrence by creating an instance override
            Event instanceOverride = createInstanceOverride(masterEvent, occurrenceDateTime, dto, user);
            return convertToDTO(instanceOverride);
        } else if ("series".equals(scope) || occurrenceDateTime == null) {
            // Update the entire series (master event)
            updateEventFields(masterEvent, dto);
            Event updatedEvent = eventRepository.save(masterEvent);
            return convertToDTO(updatedEvent);
        } else {
            // Default behavior - if it's an occurrence, update the series
            updateEventFields(masterEvent, dto);
            Event updatedEvent = eventRepository.save(masterEvent);
            return convertToDTO(updatedEvent);
        }
    }

    private void updateEventFields(Event event, CreateEventDTO dto) {
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDateTime(dto.getStartDateTime());
        event.setEndDateTime(dto.getEndDateTime());
        event.setRecurrenceRule(dto.getRecurrenceRule());
        event.setRecurrenceEndDate(dto.getRecurrenceEndDate());
        event.setRecurrenceCount(dto.getRecurrenceCount());

        // Handle tag updates
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(dto.getTagIds());
            if (tags.size() != dto.getTagIds().size()) {
                throw new IllegalArgumentException("One or more tags not found");
            }
            event.setTags(new HashSet<>(tags));
        } else {
            event.setTags(new HashSet<>());
        }
    }

    @Transactional
    public void deleteEvent(Long id, String userEmail) {
        deleteEvent(id, userEmail, "instance");
    }

    @Transactional
    public void deleteEvent(Long id, String userEmail, String scope) {
        // Find the user
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        // Determine if this is a master event ID or an occurrence ID
        OccurrenceInfo occurrenceInfo = parseOccurrenceId(id);
        Event masterEvent;
        LocalDateTime occurrenceDateTime = null;

        if (occurrenceInfo != null) {
            // This is an occurrence ID, find the master event
            Optional<Event> eventOpt = eventRepository.findById(occurrenceInfo.masterEventId);
            if (eventOpt.isEmpty()) {
                throw new IllegalArgumentException("Master event not found");
            }
            masterEvent = eventOpt.get();
            occurrenceDateTime = occurrenceInfo.occurrenceDateTime;
        } else {
            // This is a master event ID or instance override
            Optional<Event> eventOpt = eventRepository.findById(id);
            if (eventOpt.isEmpty()) {
                throw new IllegalArgumentException("Event not found");
            }
            masterEvent = eventOpt.get();
        }

        // Verify ownership
        if (!masterEvent.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied: You can only delete your own events");
        }

        // Handle different scopes for recurring events
        if ("instance".equals(scope)) {
            if (occurrenceDateTime != null) {
                // Delete a specific occurrence by adding it to excluded dates
                String currentExclusions = masterEvent.getExcludedDates();
                String newExclusion = occurrenceDateTime.toString();

                if (currentExclusions == null || currentExclusions.isEmpty()) {
                    masterEvent.setExcludedDates(newExclusion);
                } else {
                    masterEvent.setExcludedDates(currentExclusions + "," + newExclusion);
                }
                eventRepository.save(masterEvent);
            } else {
                // This is a master event or instance override
                if (masterEvent.getParentEvent() != null) {
                    // This is an instance override, just delete it
                    eventRepository.delete(masterEvent);
                } else if (masterEvent.getRecurrenceRule() != null && !masterEvent.getRecurrenceRule().isEmpty()) {
                    // This is a recurring master event, add the first occurrence to excluded dates
                    String currentExclusions = masterEvent.getExcludedDates();
                    String newExclusion = masterEvent.getStartDateTime().toString();

                    if (currentExclusions == null || currentExclusions.isEmpty()) {
                        masterEvent.setExcludedDates(newExclusion);
                    } else {
                        masterEvent.setExcludedDates(currentExclusions + "," + newExclusion);
                    }
                    eventRepository.save(masterEvent);
                } else {
                    // This is a single event, just delete it
                    eventRepository.delete(masterEvent);
                }
            }
        } else if ("series".equals(scope)) {
            // Delete the entire series
            eventRepository.delete(masterEvent);
            // Also delete any instance overrides
            eventRepository.deleteByParentEvent(masterEvent);
        } else {
            // Default to instance behavior
            if (occurrenceDateTime != null) {
                String currentExclusions = masterEvent.getExcludedDates();
                String newExclusion = occurrenceDateTime.toString();

                if (currentExclusions == null || currentExclusions.isEmpty()) {
                    masterEvent.setExcludedDates(newExclusion);
                } else {
                    masterEvent.setExcludedDates(currentExclusions + "," + newExclusion);
                }
                eventRepository.save(masterEvent);
            } else {
                eventRepository.delete(masterEvent);
            }
        }
    }

    private EventDTO convertToDTO(Event event) {
        // Convert tags to TagDTOs
        List<TagDTO> tagDTOs = event.getTags().stream()
                .map(tag -> TagDTO.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build())
                .collect(Collectors.toList());

        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .userEmail(event.getUser().getEmail())
                .recurrenceRule(event.getRecurrenceRule())
                .recurrenceEndDate(event.getRecurrenceEndDate())
                .recurrenceCount(event.getRecurrenceCount())
                .parentEventId(event.getParentEvent() != null ? event.getParentEvent().getId() : null)
                .originalStartDateTime(event.getOriginalStartDateTime())
                .excludedDates(event.getExcludedDates())
                .tags(tagDTOs)
                .build();
    }

    /**
     * Expands a recurring event into individual occurrences within the specified date range
     */
    private List<EventDTO> expandRecurringEvent(Event event, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        List<EventDTO> occurrences = new ArrayList<>();

        if (event.getRecurrenceRule() == null || event.getRecurrenceRule().isEmpty()) {
            return occurrences;
        }

        LocalDateTime eventStart = event.getStartDateTime();
        LocalDateTime eventEnd = event.getEndDateTime();
        long eventDuration = ChronoUnit.MINUTES.between(eventStart, eventEnd);

        // Parse the recurrence rule
        RecurrenceInfo recInfo = parseRecurrenceRule(event.getRecurrenceRule());
        if (recInfo == null) {
            return occurrences;
        }

        LocalDateTime currentOccurrence = eventStart;
        int occurrenceCount = 0;

        // Parse excluded dates
        List<String> excludedDatesList = new ArrayList<>();
        if (event.getExcludedDates() != null && !event.getExcludedDates().isEmpty()) {
            excludedDatesList = List.of(event.getExcludedDates().split(","));
        }

        while (currentOccurrence.isBefore(rangeEnd)) {
            // Check if we've reached the recurrence end conditions
            if (event.getRecurrenceEndDate() != null && currentOccurrence.isAfter(event.getRecurrenceEndDate())) {
                break;
            }
            if (event.getRecurrenceCount() != null && occurrenceCount >= event.getRecurrenceCount()) {
                break;
            }

            // Check if this occurrence is excluded
            String currentOccurrenceStr = currentOccurrence.toString();
            boolean isExcluded = excludedDatesList.contains(currentOccurrenceStr);

            // Check if this occurrence falls within our requested range and is not excluded
            LocalDateTime occurrenceEnd = currentOccurrence.plusMinutes(eventDuration);
            if (currentOccurrence.isBefore(rangeEnd) && occurrenceEnd.isAfter(rangeStart) && !isExcluded) {
                // Generate unique occurrence ID by combining master event ID with occurrence timestamp
                Long occurrenceId = generateOccurrenceId(event.getId(), currentOccurrence);

                // Convert tags to TagDTOs (inherit from master event)
                List<TagDTO> tagDTOs = event.getTags().stream()
                        .map(tag -> TagDTO.builder()
                                .id(tag.getId())
                                .name(tag.getName())
                                .build())
                        .collect(Collectors.toList());

                EventDTO occurrence = EventDTO.builder()
                        .id(occurrenceId)
                        .title(event.getTitle())
                        .description(event.getDescription())
                        .startDateTime(currentOccurrence)
                        .endDateTime(occurrenceEnd)
                        .userEmail(event.getUser().getEmail())
                        .recurrenceRule(event.getRecurrenceRule())
                        .recurrenceEndDate(event.getRecurrenceEndDate())
                        .recurrenceCount(event.getRecurrenceCount())
                        .parentEventId(event.getId())
                        .originalStartDateTime(currentOccurrence)
                        .excludedDates(event.getExcludedDates())
                        .tags(tagDTOs)
                        .build();
                occurrences.add(occurrence);
            }

            // Calculate next occurrence
            currentOccurrence = getNextOccurrence(currentOccurrence, recInfo);
            occurrenceCount++;

            // Safety check to prevent infinite loops
            if (occurrenceCount > 1000) {
                break;
            }
        }

        return occurrences;
    }

    /**
     * Generate a unique occurrence ID by combining the master event ID with the occurrence timestamp
     * This creates a deterministic ID that can be used to identify specific occurrences
     */
    private Long generateOccurrenceId(Long masterEventId, LocalDateTime occurrenceDateTime) {
        // Use a hash of the master event ID and occurrence timestamp to create a unique ID
        // This ensures the same occurrence always gets the same ID
        String combined = masterEventId + "_" + occurrenceDateTime.toString();
        int hashCode = combined.hashCode();
        // Avoid Math.abs() on hashCode to prevent overflow issues with Integer.MIN_VALUE
        return (long) (hashCode & 0x7FFFFFFF);
    }

    /**
     * Simple recurrence info holder
     */
    private static class RecurrenceInfo {
        String frequency; // DAILY, WEEKLY, MONTHLY
        int interval = 1; // Every N days/weeks/months
    }

    /**
     * Holder for occurrence information parsed from occurrence ID
     */
    private static class OccurrenceInfo {
        Long masterEventId;
        LocalDateTime occurrenceDateTime;

        OccurrenceInfo(Long masterEventId, LocalDateTime occurrenceDateTime) {
            this.masterEventId = masterEventId;
            this.occurrenceDateTime = occurrenceDateTime;
        }
    }

    /**
     * Parse an occurrence ID to extract the master event ID and occurrence datetime
     * Returns null if the ID is a regular master event ID
     */
    private OccurrenceInfo parseOccurrenceId(Long id) {
        // Try to find all events and check if any occurrence would generate this ID
        List<Event> allEvents = eventRepository.findAll();

        for (Event event : allEvents) {
            if (event.getRecurrenceRule() != null && !event.getRecurrenceRule().isEmpty()) {
                // Check if this ID could be generated from this event's occurrences
                LocalDateTime eventStart = event.getStartDateTime();
                RecurrenceInfo recInfo = parseRecurrenceRule(event.getRecurrenceRule());
                if (recInfo == null) continue;

                LocalDateTime currentOccurrence = eventStart;
                int occurrenceCount = 0;

                // Check up to 1000 occurrences to find a match
                while (occurrenceCount < 1000) {
                    Long generatedId = generateOccurrenceId(event.getId(), currentOccurrence);
                    if (generatedId.equals(id)) {
                        return new OccurrenceInfo(event.getId(), currentOccurrence);
                    }

                    currentOccurrence = getNextOccurrence(currentOccurrence, recInfo);
                    occurrenceCount++;

                    // Stop if we've gone too far into the future
                    if (currentOccurrence.isAfter(LocalDateTime.now().plusYears(2))) {
                        break;
                    }
                }
            }
        }

        return null; // This is a regular master event ID
    }

    /**
     * Create an instance override for a specific occurrence of a recurring event
     */
    private Event createInstanceOverride(Event masterEvent, LocalDateTime occurrenceDateTime, CreateEventDTO dto, User user) {
        // First, add the original occurrence to excluded dates
        String currentExclusions = masterEvent.getExcludedDates();
        String newExclusion = occurrenceDateTime.toString();

        if (currentExclusions == null || currentExclusions.isEmpty()) {
            masterEvent.setExcludedDates(newExclusion);
        } else {
            masterEvent.setExcludedDates(currentExclusions + "," + newExclusion);
        }
        eventRepository.save(masterEvent);

        // Create a new event for this specific occurrence
        Event instanceOverride = Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startDateTime(dto.getStartDateTime())
                .endDateTime(dto.getEndDateTime())
                .user(user)
                .parentEvent(masterEvent)
                .originalStartDateTime(occurrenceDateTime)
                .recurrenceRule(null) // Instance overrides are not recurring
                .recurrenceEndDate(null)
                .recurrenceCount(null)
                .excludedDates(null)
                .build();

        return eventRepository.save(instanceOverride);
    }

    /**
     * Parse a simple RRULE string (supports FREQ=DAILY/WEEKLY/MONTHLY with optional INTERVAL)
     */
    private RecurrenceInfo parseRecurrenceRule(String rrule) {
        if (rrule == null || rrule.isEmpty()) {
            return null;
        }

        RecurrenceInfo info = new RecurrenceInfo();
        String[] parts = rrule.split(";");

        for (String part : parts) {
            String[] keyValue = part.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                switch (key) {
                    case "FREQ":
                        info.frequency = value;
                        break;
                    case "INTERVAL":
                        try {
                            info.interval = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            info.interval = 1;
                        }
                        break;
                }
            }
        }

        return info;
    }

    /**
     * Calculate the next occurrence based on recurrence info
     */
    private LocalDateTime getNextOccurrence(LocalDateTime current, RecurrenceInfo recInfo) {
        switch (recInfo.frequency) {
            case "DAILY":
                return current.plusDays(recInfo.interval);
            case "WEEKLY":
                return current.plusWeeks(recInfo.interval);
            case "MONTHLY":
                return current.plusMonths(recInfo.interval);
            default:
                return current.plusDays(1); // Default to daily
        }
    }
}
