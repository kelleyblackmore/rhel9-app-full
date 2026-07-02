package com.kelleyblackmore.secureledger.dto;

import com.kelleyblackmore.secureledger.entity.Task;
import com.kelleyblackmore.secureledger.entity.TaskStatus;

import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        String owner,
        Instant createdAt,
        Instant updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getOwner(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
