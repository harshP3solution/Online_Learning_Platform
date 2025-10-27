package com.persistence.DTO;



import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CourseDTO> coursesTaught;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseDTO {
        private Long id;
        private String title;
        private String category;
    }
}

