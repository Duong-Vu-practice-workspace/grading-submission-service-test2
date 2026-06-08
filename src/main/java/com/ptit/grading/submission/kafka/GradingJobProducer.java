package com.ptit.grading.submission.kafka;

import com.ptit.grading.common.dto.GradingJob;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GradingJobProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Gson gson;

    @Value("${kafka.topic.grading-jobs:grading-jobs}")
    private String topic;

    public void send(UUID submissionId, UUID assignmentId, UUID studentId, String minioPath) {
        GradingJob job = GradingJob.builder()
                .submissionId(submissionId)
                .assignmentId(assignmentId)
                .studentId(studentId)
                .minioPath(minioPath)
                .timestamp(Instant.now())
                .build();

        kafkaTemplate.send(topic, submissionId.toString(), gson.toJson(job))
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send grading job: {}", submissionId, ex);
                    } else {
                        log.info("Grading job sent: {} partition={}",
                            submissionId, result.getRecordMetadata().partition());
                    }
                });
    }
}
