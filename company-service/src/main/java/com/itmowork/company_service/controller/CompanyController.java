package com.itmowork.company_service.controller;

import com.itmowork.company_service.dto.request.CompanyRequestDto;
import com.itmowork.company_service.dto.response.CompanyResponseDto;
import com.itmowork.company_service.service.interfaces.CompanyService;
import com.itmowork.company_service.service.interfaces.UserCompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final UserCompanyService userCompanyService;

    @PostMapping("/register-company")
    public Mono<ResponseEntity<CompanyResponseDto>> createCompany(@RequestBody @Valid Mono<CompanyRequestDto> companyRequestDto){
        return companyRequestDto
                .flatMap(companyService::createCompany)
                .map(companyResponseDto ->
                        ResponseEntity.status(HttpStatus.CREATED).body(companyResponseDto));
    }

    @GetMapping("/{companyId}/{userId}")
    public Mono<Boolean> validateCompanyOwnership(@PathVariable UUID companyId, @PathVariable UUID userId){
        return userCompanyService.validateCompanyOwnership(companyId, userId);
    }
}
