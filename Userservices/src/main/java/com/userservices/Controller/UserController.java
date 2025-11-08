package com.userservices.Controller;

import com.persistence.DTO.ApiResponse;
import com.persistence.DTO.UserDTO;
import com.persistence.Entity.Course;
import com.persistence.Entity.User;
import com.userservices.Service.UserServices;
import jakarta.validation.Valid;
import org.springframework.hateoas.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServices userServices;

    public UserController(UserServices userServices) {
        this.userServices = userServices;
    }

    // 🔍 SEARCH USERS
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EntityModel<UserDTO>>>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role
    ) {
        List<UserDTO> users = userServices.searchUsers(name, email, role);

        if (users == null || users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No users found matching the criteria"));
        }

        // Add HATEOAS links
        List<EntityModel<UserDTO>> userModels = users.stream()
                .map(user -> EntityModel.of(user,
                        linkTo(methodOn(UserController.class).getById(user.getId())).withSelfRel(),
                        linkTo(methodOn(UserController.class).getAll()).withRel("all-users"),
                        linkTo(methodOn(UserController.class).getAllCoursesForUser(user.getId())).withRel("user-courses")
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok("Users fetched successfully", userModels));
    }

    // 🔹 GET ALL USERS (Admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<UserDTO>>>> getAll() {
        List<UserDTO> list = userServices.getAll();
        if (list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No users found"));
        }

        List<EntityModel<UserDTO>> userModels = list.stream()
                .map(user -> EntityModel.of(user,
                        linkTo(methodOn(UserController.class).getById(user.getId())).withSelfRel(),
                        linkTo(methodOn(UserController.class).getAll()).withRel("all-users")
                ))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UserDTO>> collectionModel =
                CollectionModel.of(userModels, linkTo(methodOn(UserController.class).getAll()).withSelfRel());

        return ResponseEntity.ok(ApiResponse.ok("All users fetched successfully", collectionModel));
    }

    // 🔹 GET USER BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<EntityModel<UserDTO>>> getById(@PathVariable Long id) {
        UserDTO dto = userServices.getById(id);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found with ID: " + id));
        }

        EntityModel<UserDTO> model = EntityModel.of(dto,
                linkTo(methodOn(UserController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAll()).withRel("all-users"),
                linkTo(methodOn(UserController.class).getAllCoursesForUser(id)).withRel("user-courses")
        );

        return ResponseEntity.ok(ApiResponse.ok("User fetched successfully", model));
    }

    // 🔹 REGISTER NEW USER
    @PostMapping("/register")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<EntityModel<User>>> register(@RequestBody @Valid User user) {
        try {
            User createdUser = userServices.create(user);

            EntityModel<User> model = EntityModel.of(createdUser,
                    linkTo(methodOn(UserController.class).getById(createdUser.getId())).withSelfRel(),
                    linkTo(methodOn(UserController.class).getAll()).withRel("all-users")
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("User registered successfully", model));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // 🔹 UPDATE USER
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<EntityModel<User>>> update(@PathVariable Long id, @RequestBody @Valid User user) {
        try {
            User updatedUser = userServices.update(id, user);

            EntityModel<User> model = EntityModel.of(updatedUser,
                    linkTo(methodOn(UserController.class).getById(id)).withSelfRel(),
                    linkTo(methodOn(UserController.class).getAll()).withRel("all-users")
            );

            return ResponseEntity.ok(ApiResponse.ok("User updated successfully", model));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // 🔹 DELETE USER
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            userServices.delete(id);
            return ResponseEntity.ok(ApiResponse.ok("User deleted successfully", null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // 🔹 GET ALL COURSES FOR A USER
    @GetMapping("/{id}/courses")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<CollectionModel<EntityModel<Course>>>> getAllCoursesForUser(@PathVariable Long id) {
        List<Course> list = userServices.getAllCoursesForUser(id);
        if (list == null || list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("No courses found for this user"));
        }

        List<EntityModel<Course>> courseModels = list.stream()
                .map(course -> EntityModel.of(course,
                        linkTo(methodOn(UserController.class).getAllCoursesForUser(id)).withSelfRel(),
                        linkTo(methodOn(UserController.class).getById(id)).withRel("user")
                ))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Course>> collectionModel =
                CollectionModel.of(courseModels, linkTo(methodOn(UserController.class).getById(id)).withRel("user"));

        return ResponseEntity.ok(ApiResponse.ok("Courses for user fetched successfully", collectionModel));
    }

    // 🔹 REGISTER USER TO COURSE
    @PostMapping("/{userId}/courses/{courseId}/register")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<EntityModel<String>>> registerUserToCourse(
            @PathVariable Long userId,
            @PathVariable Long courseId
    ) {
        try {
            userServices.registerUserToCourse(userId, courseId);

            EntityModel<String> model = EntityModel.of("User registered to course and email sent successfully",
                    linkTo(methodOn(UserController.class).getById(userId)).withRel("user"),
                    linkTo(methodOn(UserController.class).getAllCoursesForUser(userId)).withRel("user-courses")
            );

            return ResponseEntity.ok(ApiResponse.ok("User registered to course successfully", model));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
