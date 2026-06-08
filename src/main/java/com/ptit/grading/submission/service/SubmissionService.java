package com.ptit.grading.submission.service;

import com.ptit.grading.common.model.SubmissionStatus;
import com.ptit.grading.submission.kafka.GradingJobProducer;
import com.ptit.grading.submission.model.Submission;
import com.ptit.grading.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final MinioService minioService;
    private final GradingJobProducer gradingJobProducer;

    @Transactional
    public Submission submit(UUID assignmentId, UUID studentId, MultipartFile file) throws Exception {
        // 1. Validate file is zip
        if (!file.getOriginalFilename().endsWith(".zip")) {
            throw new IllegalArgumentException("File must be a zip archive");
        }

        // 2. Validate zip contains docker-compose.yml
        if (!containsDockerCompose(file)) {
            throw new IllegalArgumentException("Zip must contain docker-compose.yml");
        }

        // 3. Upload to MinIO
        String minioPath = "submissions/" + UUID.randomUUID() + ".zip";
        minioService.upload(minioPath, file);

        // 4. Save to DB
        Submission submission = Submission.builder()
                .assignmentId(assignmentId)
                .studentId(studentId)
                .minioPath(minioPath)
                .zipFileName(file.getOriginalFilename())
                .status(SubmissionStatus.PENDING)
                .latest(true)
                .build();
        submission = submissionRepository.save(submission);

        // 5. Send to Kafka
        gradingJobProducer.send(
            submission.getId(),
            assignmentId,
            studentId,
            minioPath
        );

        log.info("Submission created: {}", submission.getId());
        return submission;
    }

    public Submission getById(UUID id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }

    public Map<String, Object> getSubmissionInfo(UUID id) {
        Submission submission = getById(id);
        return Map.of(
            "id", submission.getId(),
            "assignmentId", submission.getAssignmentId(),
            "studentId", submission.getStudentId(),
            "status", submission.getStatus().name(),
            "zipFileName", submission.getZipFileName(),
            "createdAt", submission.getCreatedAt()
        );
    }

    private boolean containsDockerCompose(MultipartFile file) {
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.equals("docker-compose.yml") || name.equals("docker-compose.yaml")) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
