package com.itmowork.user_service.dto.response;

import java.util.UUID;

public record UserDeleteResponseDto(
        UUID id,
        String message
) {
}
