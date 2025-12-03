package com.itmowork.company_service.service.interfaces;

import com.itmowork.company_service.dto.request.CompanyRequestDto;
import com.itmowork.company_service.dto.response.CompanyResponseDto;
import reactor.core.publisher.Mono;

public interface CompanyService {

    Mono<CompanyResponseDto> createCompany(CompanyRequestDto companyRequestDto);
}
