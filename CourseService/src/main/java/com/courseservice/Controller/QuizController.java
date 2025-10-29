package com.courseservice.Controller;

import com.courseservice.Service.QuizService;
import com.persistence.DTO.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/add-questions")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<String>> addCourseQuizQuestions(@RequestBody @Valid QuizDTO dto) {
        quizService.addQuizQuestions(dto);
        return ResponseEntity.ok(ApiResponse.ok("Quiz questions added successfully", null));
    }

    @PostMapping("/generate/final")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<QuizDTO>> generateFinalQuizAfterCourseCompletion(
            @RequestParam Long studentId, @RequestParam Long courseId) {
        QuizDTO dto = quizService.generateFinalQuizAfterCourseCompletion(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.ok("Final quiz generated successfully", dto));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<SubmitResponseDTO>> submitQuiz(@RequestBody @Valid SubmitRequestDTO req) {
        SubmitResponseDTO response = quizService.submitQuiz(req);
        return ResponseEntity.ok(ApiResponse.ok("Quiz submitted successfully", response));
    }
}
