package com.persistence.Entity;
import jakarta.persistence.*;
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

    @ManyToOne
    @JoinColumn(name="enrollment_id", nullable=false)
    private Enrollment enrollment;

    @ManyToOne
    @JoinColumn(name="lesson_id", nullable=false)
    private Lesson lesson;

    private Boolean isComplete;
    private LocalDateTime completedAt;
}

