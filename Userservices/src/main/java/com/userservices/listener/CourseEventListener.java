package com.userservices.listener;



import com.userservices.event.CourseCreateEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CourseEventListener {
    @KafkaListener(topics = "course-created-topic", groupId = "email-service")
    public void handleCourseCreated(CourseCreateEvent event) {
        System.out.println("📘 New Course Created: " + event.getTitle());
    }
}

