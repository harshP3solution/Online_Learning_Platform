package com.userservices.event;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateGeneratedEvent {
    private Long certificateId;
    private Long studentId;
    private Long courseId;
    private String courseTitle;
    private String studentEmail;
}

