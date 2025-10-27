package com.courseservice.event;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

 public class CourseCreateEvent {
    private Long courseId;
    private String title;
    private String category;
    private Long instructorId;
}
