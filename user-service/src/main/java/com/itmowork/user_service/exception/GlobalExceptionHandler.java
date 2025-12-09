package com.itmowork.user_service.exception;

import com.itmowork.user_service.exception.exceptions.UserAlreadyExistsException;
import com.itmowork.user_service.exception.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ProblemDetail> handleValidationException(WebExchangeBindException ex,
                                                         ServerWebExchange exchange) {
        String details = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return Mono.just(ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Validation failed", details, exchange));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<ProblemDetail> handleUserAlreadyExistsException(UserAlreadyExistsException ex, ServerWebExchange exchange){
        return Mono.just(
                ProblemDetailsUtils.problemDetail(
                        HttpStatus.CONFLICT,
                        "User already exists",
                        ex.getMessage(),
                        exchange
                ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ProblemDetail> handleUserNotFoundException(UserNotFoundException ex, ServerWebExchange exchange){
        return Mono.just(
                ProblemDetailsUtils.problemDetail(
                        HttpStatus.NOT_FOUND,
                        "User not found",
                        ex.getMessage(),
                        exchange
                ));
    }
}
