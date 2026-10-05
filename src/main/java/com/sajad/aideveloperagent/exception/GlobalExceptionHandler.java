package com.sajad.aideveloperagent.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(AgentException.class)
    public ProblemDetail agent(AgentException ex) {
        var detail = ProblemDetail.forStatusAndDetail(ex.status(), ex.getMessage());
        detail.setProperty("code", ex.code());
        return detail;
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalid(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpected(Exception ex) {
        log.error("Unexpected request failure", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "The request could not be completed. Check the application logs.");
    }
}
