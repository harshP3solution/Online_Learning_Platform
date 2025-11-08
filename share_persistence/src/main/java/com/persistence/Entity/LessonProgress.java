package com.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Enrollment cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;


    @NotNull(message = "Lesson cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Builder.Default
    @NotNull(message = "Completion status cannot be null")
    @Column(nullable = false)
    private Boolean isComplete = false;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMMM dd, yyyy hh:mm a")
    @Column(name = "completed_at")
    private LocalDateTime completedAt;


    @PrePersist
    @PreUpdate
    protected void updateCompletionDate() {
        if (Boolean.TRUE.equals(this.isComplete) && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }
}
