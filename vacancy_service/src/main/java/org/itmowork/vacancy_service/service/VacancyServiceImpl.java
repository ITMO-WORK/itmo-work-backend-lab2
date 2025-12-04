package org.itmowork.vacancy_service.service;

import lombok.RequiredArgsConstructor;
import org.itmowork.vacancy_service.dto.request.VacancyCreateRequestDto;
import org.itmowork.vacancy_service.dto.request.VacancyUpdateRequestDto;
import org.itmowork.vacancy_service.dto.response.VacancyResponseDto;
import org.itmowork.vacancy_service.exception.exceptions.InvalidVacancySalaryException;
import org.itmowork.vacancy_service.exception.exceptions.VacancyNotFoundException;
import org.itmowork.vacancy_service.model.Vacancy;
import org.itmowork.vacancy_service.model.VacancyStatus;
import org.itmowork.vacancy_service.model.VacancyStatusName;
import org.itmowork.vacancy_service.repository.VacancyRepository;
import org.itmowork.vacancy_service.service.interfaces.CurrencyService;
import org.itmowork.vacancy_service.service.interfaces.VacancyService;
import org.itmowork.vacancy_service.service.interfaces.VacancyStatusService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VacancyServiceImpl implements VacancyService {

    private final VacancyRepository vacancyRepository;
    private final VacancyStatusService vacancyStatusService;
    private final CurrencyService currencyService;

    @Override
    public Page<VacancyResponseDto> getAllPublishedVacancies(Pageable pageable) {
        Page<Vacancy> vacancies = vacancyRepository.getAllPublished(pageable);

        return vacancies.map(v -> new VacancyResponseDto(
                v.getId(),
                v.getTitle(),
                v.getDescription(),
                v.getSalaryFrom(),
                v.getSalaryTo(),
                v.getStatus().getId(),
                v.getCompanyId(),
                v.getCurrency().getId()
        ));
    }

//    @Override
//    public VacancyResponseDto updateAndChangeStatus(UUID userId, Long vacancyId, VacancyUpdateRequestDto dto, VacancyStatusName newStatus) {
//        return null;
//    }
//
//    @Override
//    public VacancyResponseDto updateVacancy(UUID userId, Long vacancyId, VacancyUpdateRequestDto dto) {
//        return null;
//    }
//
//    @Override
//    public VacancyResponseDto changeStatus(UUID userId, Long vacancyId, VacancyStatusName newStatus) {
//        return null;
//    }
//
//    @Override
//    public VacancyResponseDto createVacancy(UUID userId, VacancyCreateRequestDto request, VacancyStatusName statusName) {
//        return null;
//    }

    @Override
    public Vacancy getReferenceById(UUID vacancyId) {
        return vacancyRepository.getReferenceById(vacancyId);
    }

    @Override
    public boolean existsVacancyById(UUID id) {
        return vacancyRepository.existsVacanciesById(id);
    }

    @Override
    public VacancyStatus findCurrentVacancyStatusByVacancyId(UUID id) {
        Long vacancyStatusId = vacancyRepository.findVacancyStatusById(id);
        return vacancyStatusService.findVacancyStatusById(vacancyStatusId);
    }

    @Override
    public UUID findCompanyByVacancyId(UUID vacancyId) {
        return vacancyRepository.findCompanyIdById(vacancyId);
    }

    private void validateSalaryBounds(Integer salaryFrom, Integer salaryTo) {
        if (salaryFrom != null && salaryFrom < 0) {
            throw new InvalidVacancySalaryException("salary_from must be non-negative");
        }

        if (salaryTo != null && salaryTo < 0) {
            throw new InvalidVacancySalaryException("salary_to must be non-negative");
        }

        if (salaryFrom != null && salaryTo != null && salaryFrom > salaryTo) {
            throw new InvalidVacancySalaryException("salary_from cannot be greater than salary_to");
        }
    }

    private Vacancy getAndValidateVacancy(UUID vacancyId) {
        return vacancyRepository.findById(vacancyId)
                .orElseThrow(() ->
                        new VacancyNotFoundException("Vacancy with id=" + vacancyId + " not found"));
    }

    private VacancyResponseDto buildResponse(Vacancy saved) {
        return VacancyResponseDto.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .salaryFrom(saved.getSalaryFrom())
                .salaryTo(saved.getSalaryTo())
                .statusId(saved.getStatus().getId())
                .companyId(saved.getCompanyId())
                .currencyId(saved.getCurrency().getId())
                .build();
    }

    @Override
    public String getVacancyTitle(UUID vacancyId) {
        String title = vacancyRepository.findTitleById(vacancyId);
        if (title == null) {
            throw new VacancyNotFoundException("Vacancy with id=" + vacancyId + " not found");
        }
        return title;
    }

    @Override
    public boolean isVacancyPublished(UUID vacancyId) {
        Boolean published = vacancyRepository.isPublished(vacancyId);
        if (published == null) {
            throw new VacancyNotFoundException("Vacancy with id=" + vacancyId + " not found");
        }
        return published;
    }
}
