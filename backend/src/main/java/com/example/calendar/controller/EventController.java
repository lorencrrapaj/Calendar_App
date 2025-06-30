package com.example.calendar.controller;

import com.example.calendar.dto.CreateEventDTO;
import com.example.calendar.dto.EventDTO;
import com.example.calendar.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Validated
public class EventController {

    private final EventService eventService;

    /**
     * Create a new event for the logged-in user.
     */
    @PostMapping
    public ResponseEntity<?> createEvent(
            @Valid @RequestBody CreateEventDTO dto,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated");
        }

        try {
            EventDTO created = eventService.createEvent(dto, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating the event");
        }
    }

    /**
     * Fetch all events for the logged-in user between the given start and end datetimes.
     * Required by the calendar UI to display only the visible range.
     * Optionally filter by tag ID.
     */
    @GetMapping
    public ResponseEntity<?> getEventsInRange(
            Principal principal,
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end,
            @RequestParam(value = "tagId", required = false)
            Long tagId
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated");
        }

        try {
            List<EventDTO> events = eventService.getEventsForUserInRange(
                    principal.getName(), start, end, tagId);
            return ResponseEntity.ok(events);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching events");
        }
    }

    /**
     * Update an existing event for the logged-in user.
     * Supports scope parameter for recurring events: instance, series
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody CreateEventDTO dto,
            @RequestParam(value = "scope", defaultValue = "single") String scope,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated");
        }

        try {
            EventDTO updated = eventService.updateEvent(id, dto, principal.getName(), scope);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message.contains("Event not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            } else if (message.contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the event");
        }
    }

    /**
     * Delete an existing event for the logged-in user.
     * Supports scope parameter for recurring events: instance, series
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Long id,
            @RequestParam(value = "scope", defaultValue = "instance") String scope,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated");
        }

        try {
            eventService.deleteEvent(id, principal.getName(), scope);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message.contains("Event not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            } else if (message.contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the event");
        }
    }
}
