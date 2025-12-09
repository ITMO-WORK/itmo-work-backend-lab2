package com.itmowork.user_service.service;

import com.itmowork.user_service.dto.request.UserRequestDto;
import com.itmowork.user_service.dto.response.UserDeleteResponseDto;
import com.itmowork.user_service.dto.response.UserResponseDto;
import com.itmowork.user_service.exception.exceptions.UserAlreadyExistsException;
import com.itmowork.user_service.exception.exceptions.UserNotFoundException;
import com.itmowork.user_service.model.User;
import com.itmowork.user_service.repository.UserRepository;
import com.itmowork.user_service.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Mono<UserResponseDto> createUser(UserRequestDto userRequestDto) {
        return Mono.fromCallable(() ->{
            Optional<User> userOptional = userRepository.findUserByEmail(userRequestDto.email());
            if(userOptional.isPresent()) throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
            User user = User.builder()
                    .fullName(userRequestDto.fullName())
                    .password(userRequestDto.password())
                    .email(userRequestDto.email())
                    .build();
            User savedUser = userRepository.save(user);
            return mapToUserResponseDto(savedUser);
        })
                .subscribeOn(Schedulers.boundedElastic());
    }
    @Override
    public Mono<UserResponseDto> findUserById(UUID id) {
        return Mono.fromCallable(() -> userRepository.findUserById(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    @Transactional
    public Mono<UserDeleteResponseDto> deleteUser(UUID id) {
        return Mono.fromCallable(() ->
                        transactionTemplate.execute(status -> {
                            User user = userRepository.deleteUserById(id)
                                    .orElseThrow(() -> new UserNotFoundException("Пользователь не был найден"));
                            return new UserDeleteResponseDto(user.getId(), "Пользователь был успешно удален");
                        }))
                .subscribeOn(Schedulers.boundedElastic());
    }


    private UserResponseDto mapToUserResponseDto(User user){
        return new UserResponseDto(
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );
    }

}
