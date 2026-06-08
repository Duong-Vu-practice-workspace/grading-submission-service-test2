package com.ptit.grading.submission.controller;

import com.ptit.grading.submission.model.Submission;
import com.ptit.grading.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> submit(
            @RequestParam("file") MultipartFile file,
            @RequestParam("assignmentId") UUID assignmentId,
            @RequestHeader("X-User-Id") UUID userId) throws Exception {
        Submission submission = submissionService.submit(assignmentId, userId, file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
            "id", submission.getId(),
            "assignmentId", submission.getAssignmentId(),
            "status", submission.getStatus().name(),
            "createdAt", submission.getCreatedAt()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(submissionService.getSubmissionInfo(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(Map.of("message", "TODO: implement list by student"));
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getByAssignment(@PathVariable UUID assignmentId) {
        return ResponseEntity.ok(Map.of("message", "TODO: implement list by assignment"));
    }
}
