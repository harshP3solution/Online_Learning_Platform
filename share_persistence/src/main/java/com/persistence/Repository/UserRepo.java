package com.persistence.Repository;


import com.persistence.Entity.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    Optional<User> findByResetToken(String resetToken);

    boolean existsByEmail(String email);

    boolean existsByFullName(@NotBlank String fullName);
    @Query("SELECT DISTINCT u FROM User u JOIN u.coursesTaught c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<User> findByCourseTitleContainingIgnoreCase(@Param("title") String title);

}