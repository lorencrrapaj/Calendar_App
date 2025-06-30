package com.example.calendar.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TagDTO {
    private Long id;
    private String name;
}