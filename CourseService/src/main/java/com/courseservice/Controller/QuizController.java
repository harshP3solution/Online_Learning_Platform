package com.courseservice.Controller;

import com.courseservice.Service.QuizService;
import com.persistence.DTO.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
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
    public ResponseEntity<ApiResponse<EntityModel<QuizDTO>>> addCourseQuizQuestions(@RequestBody @Valid QuizDTO dto) {
        EntityModel<QuizDTO> quizModel = quizService.addQuizQuestions(dto);
        return ResponseEntity.ok(ApiResponse.ok("Quiz questions added successfully", quizModel));
    }

    @PostMapping("/generate/final")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<EntityModel<QuizDTO>>> generateFinalQuizAfterCourseCompletion(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        EntityModel<QuizDTO> dto = quizService.generateFinalQuizAfterCourseCompletion(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.ok("Final quiz generated successfully", dto));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<EntityModel<SubmitResponseDTO>>> submitQuiz(@RequestBody @Valid SubmitRequestDTO req) {
        EntityModel<SubmitResponseDTO> response = quizService.submitQuiz(req);
        return ResponseEntity.ok(ApiResponse.ok("Quiz submitted successfully", response));
    }

    // ↓↓↓ Added for HATEOAS links to work properly ↓↓↓

    @GetMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<String>> getQuizById(@PathVariable Long quizId) {
        // dummy method for linkTo() in service
        return ResponseEntity.ok(ApiResponse.ok("Quiz details link placeholder", null));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<String>> getAllQuizzesByCourse(@PathVariable Long courseId) {
        // dummy method for linkTo() in service
        return ResponseEntity.ok(ApiResponse.ok("Course quizzes link placeholder", null));
    }

    @GetMapping("/submission/{submissionId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<String>> getSubmissionById(@PathVariable Long submissionId) {
        // dummy method for linkTo() in service
        return ResponseEntity.ok(ApiResponse.ok("Quiz submission details link placeholder", null));
    }
}
