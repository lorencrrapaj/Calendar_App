package com.example.calendar.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class CreateEventDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Start date and time is required")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date and time is required")
    private LocalDateTime endDateTime;

    // Recurrence fields
    private String recurrenceRule;
    private LocalDateTime recurrenceEndDate;
    private Integer recurrenceCount;

    // Tag IDs to associate with this event
    private List<Long> tagIds;

    // Custom getter to return defensive copy and avoid EI_EXPOSE_REP
    public List<Long> getTagIds() {
        return tagIds == null ? new ArrayList<>() : new ArrayList<>(tagIds);
    }
}
