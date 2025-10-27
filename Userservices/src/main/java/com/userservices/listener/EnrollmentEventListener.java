package com.userservices.listener;




import com.userservices.event.EnrollmentCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.userservices.Service.EmailService;

@Component
public class EnrollmentEventListener {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "enrollment-created-topic", groupId = "email-service")
    public void handleEnrollmentCreated(EnrollmentCreatedEvent event) {
        System.out.println("📩 New Enrollment for Course: " + event.getCourseTitle());
        // you can call emailService.sendRegistrationEmail(...) if needed
    }
}
