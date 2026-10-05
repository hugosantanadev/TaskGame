package com.gasmtask.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.gasmtask.user.domain.Plan;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        String timeZone,
        boolean rankingVisible,
        Instant createdAt,
        String activeTitle,
        Plan plan) {
}
