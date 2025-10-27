package com.persistence.DTO;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitResponseDTO {
    private Long submissionId;
    private Integer score;
    private Integer totalMarks;
    private String message;
    private  double percentage;
    private Boolean passed;

}
