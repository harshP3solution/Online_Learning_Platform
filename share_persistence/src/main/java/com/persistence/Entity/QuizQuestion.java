package com.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "quiz_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonIgnore
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    @JsonIgnore
    private Course course;

    @NotBlank(message = "Question text cannot be blank")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @NotBlank(message = "Options cannot be blank")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionsJson;


    @NotBlank(message = "Correct answer cannot be blank")
    @Column(nullable = false, length = 255)
    private String correctAnswer;


    @NotNull(message = "Marks cannot be null")
    @Column(nullable = false)
    private Integer marks;
}
