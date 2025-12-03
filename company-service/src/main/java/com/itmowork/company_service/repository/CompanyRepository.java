package com.itmowork.company_service.repository;

import com.itmowork.company_service.model.Company;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CompanyRepository extends ReactiveCrudRepository<Company, UUID> {

    Mono<Boolean> existsByEmail(String email);
}
