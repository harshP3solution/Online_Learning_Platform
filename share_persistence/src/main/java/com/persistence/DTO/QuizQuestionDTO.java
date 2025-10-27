package com.persistence.DTO;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionDTO {
    private Long id;
    private String questionText;
    private String optionsJson;
//  private String correctAnswer;
    // client will parse; do NOT include correctAnswer
    private Integer marks;
}