package com.persistence.Repository;

import com.persistence.Entity.Certificate;
import com.persistence.Entity.User;
import com.persistence.Entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByStudent(User student);
    List<Certificate> findByCourse(Course course);
    List<Certificate> findByEnrollment_Id(Long enrollmentId);
}

