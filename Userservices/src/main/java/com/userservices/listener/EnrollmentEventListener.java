package com.userservices.listener;




import com.userservices.event.EnrollmentCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.userservices.Service.EmailService;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

@Component
public class EnrollmentEventListener {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "enrollment-created-topic", groupId = "email-service")
    public void handleEnrollmentCreated(EnrollmentCreatedEvent event) {
        log.info("🎓 Received enrollment event: {}", event.getMessage());
        // you can call emailService.sendRegistrationEmail(...) if needed
    }
}
