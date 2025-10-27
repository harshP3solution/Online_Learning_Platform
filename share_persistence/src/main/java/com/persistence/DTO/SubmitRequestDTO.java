package com.persistence.DTO;

import lombok.*;

import java.util.Map;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitRequestDTO {
    private Long studentId;
    private Long quizId;
    // Map<questionId, submittedAnswer> or list of objects
    private Map<Long, String> answers;


}