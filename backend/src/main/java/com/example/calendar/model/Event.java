package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Recurrence fields
    @Column(columnDefinition = "TEXT")
    private String recurrenceRule; // RFC 5545 RRULE string

    @Column
    private LocalDateTime recurrenceEndDate;

    @Column
    private Integer recurrenceCount;

    // For tracking individual occurrences of recurring events
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_event_id")
    private Event parentEvent;

    @Column
    private LocalDateTime originalStartDateTime; // Original start time for this occurrence

    // For handling exclusions in recurring events
    @Column(columnDefinition = "TEXT")
    private String excludedDates; // Comma-separated list of excluded dates in ISO format

    // Many-to-many relationship with tags
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "event_tags",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // Custom getter to return defensive copy and avoid EI_EXPOSE_REP
    public Set<Tag> getTags() {
        return tags == null ? new HashSet<>() : new HashSet<>(tags);
    }
}
