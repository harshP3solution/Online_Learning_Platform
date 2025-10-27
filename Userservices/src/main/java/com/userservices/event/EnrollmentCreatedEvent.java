package com.userservices.event;

import lombok.*;
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class EnrollmentCreatedEvent {
    private Long enrollmentId;
    private Long studentId;
    private Long courseId;
    private String courseTitle;
    private String studentEmail;
}
