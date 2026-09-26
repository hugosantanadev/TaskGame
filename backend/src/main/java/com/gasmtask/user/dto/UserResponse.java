package com.gasmtask.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        String timeZone,
        boolean rankingVisible,
        Instant createdAt) {
}
