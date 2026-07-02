package com.kelleyblackmore.secureledger.dto;

import com.kelleyblackmore.secureledger.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 4000)
        String description,

        TaskStatus status
) {
}
