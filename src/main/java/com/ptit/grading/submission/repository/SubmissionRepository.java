package com.ptit.grading.submission.repository;

import com.ptit.grading.submission.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    List<Submission> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
    List<Submission> findByAssignmentIdOrderByCreatedAtDesc(UUID assignmentId);
}
