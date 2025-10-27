package com.persistence.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonCompletedEvent {
    private Long lessonId;
    private Long studentId;
    private Long enrollmentId;
    private String timestamp;
}
