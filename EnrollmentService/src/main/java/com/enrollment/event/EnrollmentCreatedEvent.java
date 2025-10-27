package com.enrollment.event;



import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentCreatedEvent {
    private Long enrollmentId;
    private Long studentId;
    private Long courseId;
    private String courseTitle;
    private String studentEmail;
}
