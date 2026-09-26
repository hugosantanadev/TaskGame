package com.gasmtask.user.mapper;

import com.gasmtask.user.domain.User;
import com.gasmtask.user.dto.UserResponse;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getTimeZone(),
                user.isRankingVisible(),
                user.getCreatedAt());
    }
}
