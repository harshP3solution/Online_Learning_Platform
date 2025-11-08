package com.persistence.DTO;



import com.persistence.Entity.Course;
import com.persistence.Entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonRequestDTO {
    @NotBlank(message = "Lesson title cannot be blank")
    private String title;

    @NotBlank(message = "Lesson content cannot be blank")
    private String content;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Instructor ID is required")
    private Long instructorId;

}
