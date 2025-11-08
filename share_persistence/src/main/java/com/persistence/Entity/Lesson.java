package com.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Associated course
    //@NotNull(message = "Course cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore
    private Course course;

      //Instructor who created this lesson
    @NotNull(message = "Instructor cannot be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    @JsonIgnore
    private User instructor;

    //  Title of lesson
    @NotBlank(message = "Lesson title cannot be blank")
    @Column(nullable = false, length = 255)
    private String title;

    //  Lesson content
    @NotBlank(message = "Lesson content cannot be blank")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Creation and update timestamps
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMMM dd, yyyy hh:mm a")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMMM dd, yyyy hh:mm a")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //  Lesson progress records
    @Builder.Default
    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<LessonProgress> progressRecords = new ArrayList<>();

    //  Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
