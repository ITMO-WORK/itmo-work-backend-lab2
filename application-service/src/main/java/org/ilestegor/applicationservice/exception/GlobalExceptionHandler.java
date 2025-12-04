package org.ilestegor.applicationservice.exception;

import org.ilestegor.applicationservice.exception.exceptions.*;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebInputException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> userNotFoundExceptionHandler(UserNotFoundException ex, ServerHttpRequest request){
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "User not found", "", request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(UserHasAlreadyAppliedException.class)
    public ResponseEntity<ProblemDetail> userHasAlreadyAppliedExceptionHandler(UserHasAlreadyAppliedException ex, ServerHttpRequest request){
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "User has already applied for current vacancy", "", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UserApplicationNotFoundException.class)
    public ResponseEntity<ProblemDetail> userApplicationNotFoundExceptionHandler(UserApplicationNotFoundException ex, ServerHttpRequest request){
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "User application not found", "", request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ApplicationStatusNotFoundException.class)
    public ResponseEntity<ProblemDetail> applicationStatusNotFoundExceptionHandler(ApplicationStatusNotFoundException ex, ServerHttpRequest request){
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Application status not found", "", request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidApplicationStatusForApplicationUpdate.class)
    public ResponseEntity<ProblemDetail> invalidApplicationStatusForApplicationUpdateExceptionHandler(InvalidApplicationStatusForApplicationUpdate ex, ServerHttpRequest request){
        ProblemDetail body = ProblemDetailsUtils.problemDetail(HttpStatus.BAD_REQUEST, "Application status is not new, cant update application", "", request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

}
