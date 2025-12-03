package com.itmowork.company_service.dto.response;

import java.util.UUID;

public record CompanyResponseDto(
        UUID id,
        String name,
        String email,
        String description,
        String statusMessage,
        UUID userId
) {
}
