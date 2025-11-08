package com.persistence.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder

public class EnrollmentDTO {
    private Long id;
    private LocalDateTime enrollmentDate;
    private float progress;
    private Boolean completed;
    private StudentDTO student;
    private CourseDTO course;

    @Data
    @Builder
    public static class StudentDTO {
        private Long id;
        private String fullName;
        private String email;

    }

    @Data
    @Builder
    public static class CourseDTO {
        private Long id;
        private String title;
        private String category;
    }
}
