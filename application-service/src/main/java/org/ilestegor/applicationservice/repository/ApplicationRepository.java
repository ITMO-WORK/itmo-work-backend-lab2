package org.ilestegor.applicationservice.repository;

import org.ilestegor.applicationservice.model.Application;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ApplicationRepository extends ReactiveCrudRepository<Application, Long> {
    Mono<Boolean> existsByUserIdAndVacancyId(UUID userId, UUID vacancyId);

    Mono<Application> findApplicationsByUserIdAndVacancyId(UUID userId, UUID vacancyId);

    Mono<Application> findById(UUID applicationId);

    Flux<Application> findAllByVacancyId(UUID vacancyId, Pageable pageable);
}
