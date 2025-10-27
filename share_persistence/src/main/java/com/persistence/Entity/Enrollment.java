package com.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false) // ensure not null
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.EAGER, optional = false) // ensure not null
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Builder.Default
    @Column(nullable = false) // always set
    private LocalDateTime enrollmentDate = LocalDateTime.now();

    @Column(nullable = true)
    private Float progress;

    @Column(nullable = true)
    private Boolean completed;

    @Column(nullable = true)
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "enrollment")
    @JsonIgnore
    private List<LessonProgress> lessonProgressList;
}
