package org.itmowork.vacancy_service.service.interfaces;

import org.itmowork.vacancy_service.dto.request.VacancyCreateRequestDto;
import org.itmowork.vacancy_service.dto.request.VacancyUpdateRequestDto;
import org.itmowork.vacancy_service.dto.response.VacancyResponseDto;
import org.itmowork.vacancy_service.model.Vacancy;
import org.itmowork.vacancy_service.model.VacancyStatus;
import org.itmowork.vacancy_service.model.VacancyStatusName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VacancyService {

//    VacancyResponseDto updateAndChangeStatus(
//            UUID userId,
//            Long vacancyId,
//            VacancyUpdateRequestDto dto,
//            VacancyStatusName newStatus
//    );
//
//    VacancyResponseDto updateVacancy(
//            UUID userId,
//            Long vacancyId,
//            VacancyUpdateRequestDto dto
//    );
//
//    VacancyResponseDto changeStatus(
//            UUID userId,
//            Long vacancyId,
//            VacancyStatusName newStatus
//    );
//
//    VacancyResponseDto createVacancy(
//            UUID userId,
//            VacancyCreateRequestDto request,
//            VacancyStatusName statusName
//    );

    Vacancy getReferenceById(Long vacancyId);
    boolean existsVacancyById(Long id);
    VacancyStatus findCurrentVacancyStatusByVacancyId(Long id);
    UUID findCompanyByVacancyId(Long vacancyId);
    Page<VacancyResponseDto> getAllPublishedVacancies(Pageable pageable);
}

