package com.userservices.listener;



import com.userservices.event.CertificateGeneratedEvent;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

@Component
public class CertificateEventListener {

    @KafkaListener(topics = "certificate-generated-topic", groupId = "email-service")
    public void handleCertificateGenerated(CertificateGeneratedEvent event) {
        System.out.println("🏅 Certificate generated for " + event.getStudentEmail());
    }
}
