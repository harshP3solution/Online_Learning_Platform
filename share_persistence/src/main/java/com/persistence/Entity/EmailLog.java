package com.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore
    private Course course;


    @Email
    @NotBlank(message = "Recipient email cannot be blank")
    @Column(name = "recipient_email", nullable = false, length = 150)
    private String recipientEmail;

    @NotBlank(message = "Email subject cannot be blank")
    @Column(nullable = false, length = 200)
    private String subject;


    @NotBlank(message = "Email body cannot be blank")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status cannot be null")
    @Column(nullable = false, length = 20)
    private Status status;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMMM dd, yyyy hh:mm a")
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;


    @PrePersist
    protected void onSend() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }

    public enum Status {
        SENT, FAILED
    }
}
