package com.userservices.FeignClient;


import com.persistence.Entity.Course;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "course-service", url = "http://localhost:8082")
public interface CourseClient {

    @GetMapping("/api/courses")
    List<Course> getAllCourses();
}