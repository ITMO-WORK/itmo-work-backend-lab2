package org.ilestegor.applicationservice.service;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.dto.ApplicationDto;
import org.ilestegor.applicationservice.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.dto.request.ApplicationStatusUpdateRequestDto;
import org.ilestegor.applicationservice.dto.response.ApplicationCreateResponseDto;
import org.ilestegor.applicationservice.dto.response.ApplicationStatusUpdateResponseDto;
import org.ilestegor.applicationservice.exception.exceptions.*;
import org.ilestegor.applicationservice.infrastructure.feign.user.UserClient;
import org.ilestegor.applicationservice.infrastructure.feign.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.mapper.ApplicationMapper;
import org.ilestegor.applicationservice.model.Application;
import org.ilestegor.applicationservice.model.ApplicationStatusName;
import org.ilestegor.applicationservice.repository.ApplicationRepository;
import org.ilestegor.applicationservice.service.interfaces.ApplicationService;
import org.ilestegor.applicationservice.service.interfaces.ApplicationStatusService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    private final UserClient userClient;

    private final ApplicationMapper applicationMapper;

    private final ApplicationStatusService applicationStatusService;

    @Override
    public Mono<ApplicationCreateResponseDto> createApplication(UUID id, UUID userId, ApplicationCreateRequestDto applicationCreateRequestDto) {
        // TODO check vacancy existence

        //TODO check vacancy is published

        return checkUserExists(userId).then(checkUserHasNotApplied(userId, id)).then(createAndSaveApplication(userId, id, applicationCreateRequestDto));
    }

    @Override
    public Mono<ApplicationCreateResponseDto> updateApplication(UUID id, UUID userId, ApplicationCreateRequestDto applicationCreateRequestDto) {
        // TODO check if vacancy is still opened
        return checkUserExists(userId).then(checkUserHasAlreadyAppliedForVacancy(userId, id)).then(updateAndSaveApplication(id, userId, applicationCreateRequestDto));
    }

    //Only for admins
    @Override
    public Mono<ApplicationStatusUpdateResponseDto> updateApplicationStatus(UUID applicationId, UUID userId, ApplicationStatusUpdateRequestDto applicationStatusUpdateRequestDto) {
        // TODO validate company ownership
        return checkUserExists(userId).then(applicationRepository.findById(applicationId)
                .switchIfEmpty(Mono.error(new UserApplicationNotFoundException())))
                .flatMap(application ->
                        applicationStatusService.findApplicationStatusByApplicationStatusName(applicationStatusUpdateRequestDto.applicationStatusName())
                                .switchIfEmpty(Mono.error(new ApplicationStatusNotFoundException()))
                                .flatMap(status -> {
                                    application.setStatus(status.getId());
                                    application.setUpdatedAt(LocalDateTime.now());
                                    return applicationRepository.save(application)
                                            .map(saved -> new ApplicationStatusUpdateResponseDto(status.getApplicationStatusName().getValue(), saved.getUpdatedAt()));
                                }));
    }

    @Override
    public Mono<Page<ApplicationDto>> getAllApplicationsByVacancyId(UUID vacancyId, UUID userId, Pageable pageable) {
        // TODO check vacancy exists
        // TODO if vacancy exists then i need to get vacancy title
        // TODO check user belongs to company
        // TODO get user fullname to construct DTO for response
        return checkUserExists(userId)
                .thenMany(applicationRepository.findAllByVacancyId(vacancyId, pageable)).
                flatMap(application -> checkUserExists(application.getUserId())
                        .map(user -> {
                    ApplicationDto applicationDto = applicationMapper.fromApplicationtoApplicationDto(application);
                            System.out.println(user.fullName());
                    return applicationDto.toBuilder().userFullName(user.fullName()).build();
                }))
                .collectList()
                .zipWith(applicationRepository.count())
                .map(res -> new PageImpl<>(res.getT1(), pageable, res.getT2()));

//                .map(applicationMapper::fromApplicationtoApplicationDto)
//                // collects all the elements from flux then turns them into List<> then emits Mono<List<>>
//                .collectList().zipWith(applicationRepository.count())
//                .map(res -> new PageImpl<>(res.getT1(), pageable, res.getT2()));
    }

    private Mono<ApplicationCreateResponseDto> updateAndSaveApplication(UUID vacancyId, UUID userId, ApplicationCreateRequestDto applicationCreateRequestDto){
        return applicationRepository.findApplicationsByUserIdAndVacancyId(userId, vacancyId).switchIfEmpty(Mono.error(new UserApplicationNotFoundException()))
                .flatMap(application -> {
                    applicationMapper.update(application, applicationCreateRequestDto);
                    application.setUpdatedAt(LocalDateTime.now());
                    return applicationRepository.save(application);
                }).flatMap(savedApplication ->
                    applicationStatusService.findApplicationStatusByApplicationStatusId(savedApplication.getStatus())
                            .switchIfEmpty(Mono.error(new ApplicationStatusNotFoundException()))
                            .flatMap(status -> {
                                if (!status.getApplicationStatusName().equals(ApplicationStatusName.NEW))
                                    return Mono.error(new InvalidApplicationStatusForApplicationUpdate());
                                var res =  new ApplicationCreateResponseDto(savedApplication.getId(), status.getApplicationStatusName().getValue(), savedApplication.getCreatedAt());
                                return Mono.just(res);
                            })
                );
    }

    private Mono<Boolean> checkUserHasAlreadyAppliedForVacancy(UUID userId, UUID vacancyId){
        return applicationRepository.existsByUserIdAndVacancyId(userId, vacancyId).flatMap(exists ->
        {
            if (Boolean.TRUE.equals(exists)) {
                return Mono.just(true);
            }
            return Mono.error(new UserApplicationNotFoundException());
        });
    }

    private Mono<UserResponseDto> checkUserExists(UUID userId){
        return Mono.fromCallable(() -> userClient.isUserExistsById(userId)).subscribeOn(Schedulers.boundedElastic())
                .switchIfEmpty(Mono.error(new UserNotFoundException()));
    }



    private Mono<Void> checkUserHasNotApplied(UUID userId, UUID vacancyId){
        return applicationRepository.existsByUserIdAndVacancyId(userId, vacancyId).flatMap(
                exists -> {
                    if (exists) return Mono.error(new UserHasAlreadyAppliedException());
                    return Mono.empty();
                }
        );
    }

    private Mono<ApplicationCreateResponseDto> createAndSaveApplication(UUID userId, UUID vacancyId, ApplicationCreateRequestDto applicationCreateRequestDto){
        return applicationStatusService.findApplicationStatusByApplicationStatusName(ApplicationStatusName.NEW)
                .switchIfEmpty(Mono.error(new ApplicationStatusNotFoundException()))
                .flatMap(status -> {
                    Application application = Application.builder().coverLetter(applicationCreateRequestDto.coverLetter())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .status(status.getId())
                            .userId(userId)
                            .vacancyId(vacancyId).build();

                    return applicationRepository.save(application).map(saved -> new ApplicationCreateResponseDto(
                            saved.getId(),
                            status.getApplicationStatusName().getValue(),
                            saved.getCreatedAt()
                    ));
                });
    }
}
