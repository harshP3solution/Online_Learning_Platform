package com.persistence.DTO;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CourseRequestDTO {

    @NotNull
    private String title;

    private String description;

    private String category;

    @NotNull
    private Long instructorId; // Only ID of instructor is passed
}
