package com.itmowork.company_service.service;

import com.itmowork.company_service.client.UserClient;
import com.itmowork.company_service.dto.request.CompanyRequestDto;
import com.itmowork.company_service.dto.request.UserRequestDto;
import com.itmowork.company_service.dto.response.CompanyResponseDto;
import com.itmowork.company_service.dto.response.UserResponseDto;
import com.itmowork.company_service.exception.exceptions.CompanyAlreadyExistsException;
import com.itmowork.company_service.exception.exceptions.UserClientException;
import com.itmowork.company_service.model.Company;
import com.itmowork.company_service.model.CompanyStatus;
import com.itmowork.company_service.model.CompanyStatusName;
import com.itmowork.company_service.model.UserCompany;
import com.itmowork.company_service.repository.CompanyRepository;
import com.itmowork.company_service.service.interfaces.CompanyService;
import com.itmowork.company_service.service.interfaces.CompanyStatusService;
import com.itmowork.company_service.service.interfaces.UserCompanyService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    private final CompanyStatusService companyStatusService;

    private final UserClient userClient;

    private final UserCompanyService userCompanyService;


    @Override
    @Transactional
    public Mono<CompanyResponseDto> createCompany(CompanyRequestDto companyRequestDto) {
        return companyRepository.existsByEmail(companyRequestDto.email())
                .flatMap(isExists -> {
                            if(isExists){
                                return Mono.error(new CompanyAlreadyExistsException("Компания с таким email уже существует"));
                            }
                            Mono<CompanyStatus> companyStatusMono = companyStatusService.
                                    findCompanyStatusByCompanyStatusName(CompanyStatusName.PENDING_VERIFICATION);


                            Mono<UserResponseDto> userResponseDtoMono = createRemoteUser(
                                    new UserRequestDto(
                                            companyRequestDto.ownerFullName(),
                                            companyRequestDto.ownerPassword(),
                                            companyRequestDto.ownerEmail()
                                    )
                            );

                            return Mono.zip(companyStatusMono, userResponseDtoMono);
                        }
                )
                .flatMap(tuple -> {
                    CompanyStatus companyStatus = tuple.getT1();
                    UserResponseDto userResponseDto = tuple.getT2();

                    Company company = getCompany(companyRequestDto, companyStatus);

                    return companyRepository.save(company)
                            .map(savedCompany -> Tuples.of(savedCompany, userResponseDto));
                })
                .flatMap(tuple -> {
                    Company savedCompany = tuple.getT1();
                    UserResponseDto userResponseDto = tuple.getT2();

                   UserCompany userCompany = new UserCompany();
                   userCompany.setCompanyId(savedCompany.getId());
                   userCompany.setUserId(userResponseDto.id());

                   return userCompanyService.saveUserCompany(userCompany);

                })
                .map(userCompany -> mapToCompanyResponseDto(companyRequestDto, userCompany));
    }

    private Mono<UserResponseDto> createRemoteUser(UserRequestDto userRequestDto){
        return Mono.fromCallable(() -> userClient.createUser(userRequestDto))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(FeignException.class, e ->
                        {
                            HttpStatus status = HttpStatus.resolve(e.status());
                            if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

                            return Mono.error(new UserClientException(e.getMessage(), status));
                        });
    }

    private Company getCompany(CompanyRequestDto companyRequestDto, CompanyStatus companyStatus){
        Company company = new Company();
        company.setName(companyRequestDto.name());
        company.setEmail(companyRequestDto.email());
        company.setDescription(companyRequestDto.description());
        company.setStatusId(companyStatus.getId());
        return company;
    }

    private CompanyResponseDto mapToCompanyResponseDto(CompanyRequestDto companyRequestDto, UserCompany userCompany){
        return new CompanyResponseDto(
                userCompany.getCompanyId(),
                companyRequestDto.name(),
                companyRequestDto.email(),
                companyRequestDto.description(),
                "Компания зарегистрирована. Ожидайте проверки администратора.",
                userCompany.getUserId()
        );
    }
}
