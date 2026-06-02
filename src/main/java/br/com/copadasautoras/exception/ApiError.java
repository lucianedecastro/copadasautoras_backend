package br.com.copadasautoras.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String erro,
        String path
) {}
