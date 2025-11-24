package com.jartiste.smartshop.presentation.advice;


import com.jartiste.smartshop.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.time.Instant;


@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String TIMESTAMP = "timestamp";
    private static final String PATH = "path";

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGlobalException(Exception ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage()
                );

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail("An un expected error occured: " + ex.getMessage());
        problemDetail.setProperty(PATH, request.getContextPath());
        problemDetail.setProperty(TIMESTAMP, Instant.now());


        return problemDetail;
    }


    @ExceptionHandler(BusinessLogicViolation.class)
    public ProblemDetail handleBusinessLogicViolation(BusinessLogicViolation ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );

        problemDetail.setTitle("Business Logic Validation");
        problemDetail.setDetail("A business logic error occurred: " + ex.getMessage());
        problemDetail.setProperty(PATH, request.getContextPath());
        problemDetail.setProperty(TIMESTAMP, Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(ValidationException ex, WebRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        problemDetail.setTitle("Validation Error");
        problemDetail.setDetail("A validation Error occurred: " + ex.getMessage());
        problemDetail.setProperty(PATH, request.getContextPath());
        problemDetail.setProperty(TIMESTAMP, Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(ResourceNotFound.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFound ex, WebRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setDetail("Resource Not Found: " + ex.getMessage());
        problemDetail.setProperty(PATH, request.getContextPath());
        problemDetail.setProperty(TIMESTAMP, Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(UnAuthorizedException.class)
    public ProblemDetail handleUnauthorizedException(UnAuthorizedException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );

        problemDetail.setTitle("UnAuthorized");
        problemDetail.setDetail("UnAuthorized Action: " + ex.getMessage());
        problemDetail.setProperty(PATH, request.getContextPath());
        problemDetail.setProperty(TIMESTAMP, Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(ForbbidenException.class)
    public ProblemDetail handleForbiddenException(ForbbidenException ex, WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );

        problemDetail.setTitle("Forbidden Action");
        problemDetail.setDetail("Forbidden Action: " + ex.getMessage());
        problemDetail.setProperty(PATH, request.getContextPath());
        problemDetail.setProperty(TIMESTAMP, Instant.now());

        return problemDetail;
    }
}
