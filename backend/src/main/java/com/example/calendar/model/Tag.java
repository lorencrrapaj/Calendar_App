package com.example.calendar.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "name"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private Set<Event> events = new HashSet<>();

    // Custom getter to return defensive copy and avoid EI_EXPOSE_REP
    public Set<Event> getEvents() {
        return events == null ? new HashSet<>() : new HashSet<>(events);
    }
}
