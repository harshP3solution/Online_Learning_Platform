package com.persistence.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class CertificateDTO {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime completionDate;
    private Long enrollmentId;
    private Long studentId;
    private String studentEmail;
    private Long courseId;
    private String courseTitle;
}

