package com.persistence.DTO;


import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private InstructorDTO instructor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstructorDTO {
        private Long id;
        private String fullName;
        private String email;
        private String role;
    }
}
