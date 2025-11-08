package com.userservices.Service;

import com.persistence.DTO.CourseDTO;
import com.persistence.DTO.UserDTO;
import com.persistence.Entity.Course;
import com.persistence.Entity.User;
import com.persistence.Repository.UserRepo;
import com.persistence.Repository.CourseRepository;
import com.userservices.FeignClient.CourseClient;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServices {

    private final UserRepo userRepo;
    private final CourseRepository courseRepo;
    private final CourseClient courseClient;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public UserServices(CourseRepository courseRepo, UserRepo userRepo,
                        CourseClient courseClient, EmailService emailService,
                        PasswordEncoder passwordEncoder) {
        this.courseRepo = courseRepo;
        this.userRepo = userRepo;
        this.courseClient = courseClient;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @PersistenceContext
    private EntityManager entityManager;

    // ✅ Dynamic search using CriteriaBuilder (name, email, role)
    public List<UserDTO> searchUsers(String name, String email, String role) {
        String nameKey = (name == null ? "" : name.trim().toLowerCase(Locale.ROOT));
        String emailKey = (email == null ? "" : email.trim().toLowerCase(Locale.ROOT));
        String roleKey = (role == null ? "" : role.trim().toUpperCase(Locale.ROOT));

        String namePattern = "%" + nameKey + "%";
        String emailPattern = "%" + emailKey + "%";

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        List<Predicate> predicates = new ArrayList<>();

        if (!nameKey.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("fullName")), namePattern));
        }

        if (!emailKey.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("email")), emailPattern));
        }

        if (!roleKey.isEmpty()) {
            predicates.add(cb.equal(root.get("role"), User.Role.valueOf(roleKey)));
        }

        cq.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
        List<User> users = entityManager.createQuery(cq).getResultList();

        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ Search users by course title
    public List<UserDTO> searchByCourseTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return userRepo.findAll()
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        // Find users teaching courses that contain the title
        List<User> users = userRepo.findByCourseTitleContainingIgnoreCase(title.trim());

        // Filter the courses to include only those matching the title
        return users.stream()
                .map(user -> {
                    List<Course> filteredCourses = user.getCoursesTaught().stream()
                            .filter(c -> c.getTitle() != null &&
                                    c.getTitle().toLowerCase().contains(title.toLowerCase()))
                            .collect(Collectors.toList());
                    user.setCoursesTaught(filteredCourses);
                    return convertToDTO(user);
                })
                .collect(Collectors.toList());
    }

    // ✅ Return all users
    public List<UserDTO> getAll() {
        return userRepo.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    // ✅ Return single user by ID
    public UserDTO getById(Long id) {
        return userRepo.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    // ✅ Convert Entity → DTO (made public for controller compatibility)
    public UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .coursesTaught(user.getCoursesTaught() != null
                        ? user.getCoursesTaught().stream()
                        .map(course -> CourseDTO.builder()
                                .id(course.getId())
                                .title(course.getTitle())
                                .description(course.getDescription())
                                .category(course.getCategory())
                                .createdAt(course.getCreatedAt())
                                .updatedAt(course.getUpdatedAt())
                                .instructor(
                                        CourseDTO.InstructorDTO.builder()
                                                .id(user.getId())
                                                .fullName(user.getFullName())
                                                .email(user.getEmail())
                                                .role(user.getRole().name())
                                                .build()
                                )
                                .build())
                        .collect(Collectors.toList())
                        : null)
                .build();
    }

    // ✅ Create user
    public User create(User user) {
        return userRepo.save(user);
    }

    // ✅ Update user with email/name validation
    public User update(Long id, User updatedUser) {
        return userRepo.findById(id).map(existingUser -> {

            if (!existingUser.getEmail().equals(updatedUser.getEmail()) &&
                    userRepo.existsByEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException("User already registered with this email");
            }

            if (!existingUser.getFullName().equals(updatedUser.getFullName()) &&
                    userRepo.existsByFullName(updatedUser.getFullName())) {
                throw new IllegalArgumentException("User already registered with this username");
            }

            existingUser.setFullName(updatedUser.getFullName());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setRole(updatedUser.getRole());

            if (updatedUser.getPasswordHash() != null && !updatedUser.getPasswordHash().isEmpty()) {
                existingUser.setPasswordHash(passwordEncoder.encode(updatedUser.getPasswordHash()));
            }

            return userRepo.save(existingUser);
        }).orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));
    }

    // ✅ Delete user
    public void delete(Long id) {
        userRepo.deleteById(id);
    }

    // ✅ Get all courses for a user (kept same, ready for HATEOAS controller wrapping)
    public List<Course> getAllCoursesForUser(Long userId) {
        return courseClient.getAllCourses();
    }

    // ✅ Register user to course + email notification
    public void registerUserToCourse(Long userId, Long courseId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        user.getCoursesTaught().add(course);
        userRepo.save(user);

        emailService.sendRegistrationEmail(user, course);
    }
}
