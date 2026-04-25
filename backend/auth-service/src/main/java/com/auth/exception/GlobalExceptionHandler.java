package com.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(Exception ex, HttpServletRequest req) {
        return new ApiError(Instant.now(), 400, "Bad Request", "Validation failed", req.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequest(BadRequestException ex, HttpServletRequest req) {
        return new ApiError(Instant.now(), 400, "Bad Request", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(NotFoundException ex, HttpServletRequest req) {
        return new ApiError(Instant.now(), 404, "Not Found", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError unauthorized(Exception ex, HttpServletRequest req) {
        return new ApiError(Instant.now(), 401, "Unauthorized", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler({ForbiddenException.class, DisabledException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError forbidden(Exception ex, HttpServletRequest req) {
        return new ApiError(Instant.now(), 403, "Forbidden", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError generic(Exception ex, HttpServletRequest req) {
        return new ApiError(Instant.now(), 500, "Internal Server Error", "Unexpected error", req.getRequestURI());
    }
}