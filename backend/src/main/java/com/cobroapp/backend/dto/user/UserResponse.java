package com.cobroapp.backend.dto.user;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String email,
        String status,
        OffsetDateTime createdAt
) {
}