package com.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @NotNull(message = "Student cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;


    @NotNull(message = "Course cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;


    @Column(name = "enrollment_date", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMMM dd, yyyy hh:mm a")
    private LocalDateTime enrollmentDate;


    @Column
    private Float progress;


    @Column
    private Boolean completed;

    @Column(name = "completed_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMMM dd, yyyy hh:mm a")
    private LocalDateTime completedAt;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMMM dd, yyyy hh:mm a")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMMM dd, yyyy hh:mm a")
    private LocalDateTime updatedAt;


    @Builder.Default
    @OneToMany(mappedBy = "enrollment")
    @JsonIgnore
    private List<LessonProgress> lessonProgressList = new ArrayList<>();


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.enrollmentDate == null) {
            this.enrollmentDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
