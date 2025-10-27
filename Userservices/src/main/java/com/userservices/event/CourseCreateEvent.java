package com.userservices.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCreateEvent {
        private Long courseId;
        private String title;
        private String category;
        private Long instructorId;


}
