package com.example.assets.interfaces.handler;

import com.example.assets.domain.exception.ApplicationErrorCodes;
import com.example.assets.domain.exception.ControlledErrorException;
import com.example.assets.domain.exception.GenericErrorCodes;
import com.example.assets.interfaces.exception.HttpErrorStatusResolver;
import com.example.assets.interfaces.model.ErrorResponseDto;
import com.example.assets.interfaces.model.ValidationError;
import com.example.assets.interfaces.tracing.TraceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.MissingRequestValueException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    //Check if possible to manage in interface layer instead of domain layer
    private final HttpErrorStatusResolver httpErrorStatusResolver;
    private final TraceUtils traceUtils;

    @ExceptionHandler(ControlledErrorException.class)
    public Mono<ResponseEntity<ErrorResponseDto>> handleControlledException(final ControlledErrorException ex) {
        if (ex.getHttpStatus().is5xxServerError()) {
            log.error(
                    "ControlledException {} with message={}, returning response {}",
                    ex.getErrorCode().getCode(),
                    ex.getMessage(),
                    ex.getHttpStatus().value());
        } else {
            log.warn(
                    "ControlledException {} with message={}, returning response {}",
                    ex.getErrorCode().getCode(),
                    ex.getMessage(),
                    ex.getHttpStatus().value());
        }
        final ErrorResponseDto errorResponseDto = new ErrorResponseDto(ex.getErrorCode().getCode(),
                ex.getMessage(), traceUtils.getCurrentTraceId(), null, LocalDateTime.now());
        return Mono.just(new ResponseEntity<>(errorResponseDto, ex.getHttpStatus()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ErrorResponseDto> handleNoResourceFoundException(final NoResourceFoundException ex,
                                                                 final ServerHttpRequest request) {
        final String message = format("Invalid endpoint path: %s. Review the path", request.getPath());
        log.warn(message);
        return Mono.just(new ErrorResponseDto(ApplicationErrorCodes.WRONG_PATH.getCode(),
                message, traceUtils.getCurrentTraceId(), null, LocalDateTime.now()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ErrorResponseDto> handleBeanValidationException(final WebExchangeBindException ex) {

        // Construimos la lista de errores de campo
        List<ValidationError> fieldErrors = ex.getFieldErrors().stream()
                .map(err -> new ValidationError(err.getField(), err.getDefaultMessage()))
                .toList();

        // Mensaje general resumido
        String message = "Malformed body. Validation failed for " + fieldErrors.size() + " field(s).";

        log.warn("Invalid request. Validation failed: {}", fieldErrors);

        ErrorResponseDto response = new ErrorResponseDto(
                ApplicationErrorCodes.WRONG_BODY.getCode(),
                message,
                traceUtils.getCurrentTraceId(),
                fieldErrors,
                LocalDateTime.now()
        );

        return Mono.just(response);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingRequestValueException.class)
    public Mono<ErrorResponseDto> handleMalformedUrlException(final Exception ex) {
        final String message = format
                ("Malformed URL. %s:", ex.getMessage());
        log.warn("Invalid URL. Please check proper format {}", ex.getMessage());
        return Mono.just(new ErrorResponseDto(ApplicationErrorCodes.INVALID_URL.getCode(),
                message, traceUtils.getCurrentTraceId(), null, LocalDateTime.now()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Mono<ErrorResponseDto> handleArgumentMismatchException(final Exception ex) {
        final String message = format
                ("Malformed URL. %s:", ex.getMessage());
        log.error("Invalid parameter types for provided URL. Please check proper format {}", ex.getMessage());
        return Mono.just(new ErrorResponseDto(ApplicationErrorCodes.MALFORMED_URL.getCode(),
                message, traceUtils.getCurrentTraceId(), null, LocalDateTime.now()));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Mono<ErrorResponseDto> handleBugException(final Exception ex) {
        final String message = format
                ("There was an unexpected error in the application [BUG]: %s, please contact support", ex.getMessage());
        log.error("Application error in {}, stack trace: \n %s", ex.getClass().getName(), ex);
        return Mono.just(new ErrorResponseDto(GenericErrorCodes.GENERIC_ERROR_BUG.getCode(),
                message, traceUtils.getCurrentTraceId(), null, LocalDateTime.now()));
    }

}
