package com.example.calendar.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String userEmail; // Instead of full User object, just include email

    // Recurrence fields
    private String recurrenceRule;
    private LocalDateTime recurrenceEndDate;
    private Integer recurrenceCount;
    private Long parentEventId;
    private LocalDateTime originalStartDateTime;
    private String excludedDates;

    // Tags associated with this event
    private List<TagDTO> tags;

    // Custom getter to return defensive copy and avoid EI_EXPOSE_REP
    public List<TagDTO> getTags() {
        return tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }
}
