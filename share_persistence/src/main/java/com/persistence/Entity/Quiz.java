package com.persistence.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link quiz to Course (not Lesson)
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer totalMarks;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // One quiz has many questions
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
    private List<QuizQuestion> questions;

    // One quiz can have many submissions
    @OneToMany(mappedBy = "quiz")
    private List<QuizSubmission> submissions;
}
