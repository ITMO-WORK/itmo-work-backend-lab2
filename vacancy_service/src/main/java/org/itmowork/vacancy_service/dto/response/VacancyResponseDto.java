package org.itmowork.vacancy_service.dto.response;

import lombok.Builder;
import java.util.UUID;

@Builder
public record VacancyResponseDto(
        Long id,
        String title,
        String description,
        Integer salaryFrom,
        Integer salaryTo,
        Long statusId,
        UUID companyId,     // ← ЗАМЕНЕНО
        Long currencyId
) {}
