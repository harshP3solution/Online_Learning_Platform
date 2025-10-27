package com.persistence.DTO;

import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDTO {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private Integer totalMarks;
    private List<QuizQuestionDTO> questions;
}