package com.userservices.Service;


import com.persistence.Entity.Course;
import com.persistence.Entity.EmailLog;
import com.persistence.Entity.User;
import com.persistence.Repository.EmailLogRepository;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private final EmailLogRepository emailLogRepository;

    public EmailService(JavaMailSender mailSender, EmailLogRepository emailLogRepository) {
        this.mailSender = mailSender;
        this.emailLogRepository = emailLogRepository;
    }

    public void sendRegistrationEmail(User user, Course course) {
        String subject = "Course Registration Successful";
        String body = "Thank you " + user.getFullName() +
                ", you have successfully registered for " + course.getTitle() + ".";

        EmailLog emailLog = EmailLog.builder()
                .user(user)
                .course(course)
                .recipientEmail(user.getEmail())
                .subject(subject)
                .body(body)
                .sentAt(LocalDateTime.now())
                .status(EmailLog.Status.SENT)
                .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            emailLogRepository.save(emailLog);
        } catch (Exception e) {
            emailLog.setStatus(EmailLog.Status.FAILED);
            emailLog.setErrorMessage(e.getMessage());
            emailLogRepository.save(emailLog);
        }
    }
}
