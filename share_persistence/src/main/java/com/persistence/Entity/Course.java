package com.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Course title is required")
    @Size(max = 200, message = "Course title must be less than 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Course description is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    @JsonIgnore
    private User instructor;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMMM dd, yyyy hh:mm a")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMMM dd, yyyy hh:mm a")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "course")
    @JsonIgnore
    private List<Lesson> lessons = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "course")
    @JsonIgnore
    private List<Enrollment> enrollments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "course")
    @JsonIgnore
    private List<Certificate> certificates = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
