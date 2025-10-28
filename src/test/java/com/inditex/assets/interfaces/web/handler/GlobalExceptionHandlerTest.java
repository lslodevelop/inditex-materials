package com.inditex.assets.interfaces.web.handler;

import com.inditex.assets.domain.exception.ApplicationErrorCodes;
import com.inditex.assets.domain.exception.ControlledErrorException;
import com.inditex.assets.domain.exception.GenericErrorCodes;
import com.inditex.assets.interfaces.web.model.ErrorResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.MissingRequestValueException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleControlledException5xxTest() {
        // given
        final ControlledErrorException controlledErrorException = new ControlledErrorException(
                GenericErrorCodes.GENERIC_ERROR_BUG,
                "Server exploded",
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        // when
        final Mono<ResponseEntity<ErrorResponseDto>> result = handler.handleControlledException(controlledErrorException);

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().code()).isEqualTo(GenericErrorCodes.GENERIC_ERROR_BUG.getCode());
                    assertThat(response.getBody().message()).contains("Server exploded");
                })
                .verifyComplete();
    }

    @Test
    void handleControlledExceptionTest() {
        // given
        final ControlledErrorException controlledErrorException = new ControlledErrorException(
                ApplicationErrorCodes.WRONG_BODY,
                "Controlled error occurred",
                HttpStatus.BAD_REQUEST
        );

        // when
        final Mono<ResponseEntity<ErrorResponseDto>> result = handler.handleControlledException(controlledErrorException);

        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().code()).isEqualTo(ApplicationErrorCodes.WRONG_BODY.getCode());
                    assertThat(response.getBody().message()).isEqualTo("Controlled error occurred");
                    assertThat(response.getBody().timestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void handleNoResourceFoundExceptionTest() {
        // given
        final ServerHttpRequest serverHttpRequest = MockServerHttpRequest.get("/resource/1234").build();

        // when
        final Mono<ErrorResponseDto> result = handler.handleNoResourceFoundException(serverHttpRequest);

        // then
        StepVerifier.create(result)
                .assertNext(body -> {
                    assertThat(body.code()).isEqualTo(ApplicationErrorCodes.WRONG_PATH.getCode());
                    assertThat(body.message()).contains("/resource/1234");
                    assertThat(body.timestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void handleBeanValidationExceptionTest() {
        // given
        final WebExchangeBindException webExchangeBindException = new WebExchangeBindException(
                new MethodParameter(this.getClass().getDeclaredMethods()[0], -1),
                new BeanPropertyBindingResult(new Object(), "object")
        );
        webExchangeBindException.addError(new FieldError("object", "field1", "must not be null"));
        webExchangeBindException.addError(new FieldError("object", "field2", "must not be empty"));

        // when
        final Mono<ErrorResponseDto> result = handler.handleBeanValidationException(webExchangeBindException);

        // then
        StepVerifier.create(result)
                .assertNext(body -> {
                    assertThat(body.code()).isEqualTo(ApplicationErrorCodes.WRONG_BODY.getCode());
                    assertThat(body.message()).contains("2 field(s)");
                    assertThat(body.errors()).hasSize(2);
                    assertThat(body.errors().get(0).field()).isEqualTo("field1");
                    assertThat(body.errors().get(1).field()).isEqualTo("field2");
                })
                .verifyComplete();
    }

    @Test
    void handleMalformedUrlExceptionTest() {
        // given
        final MissingRequestValueException missingRequestValueException = mock(MissingRequestValueException.class);
        when(missingRequestValueException.getMessage()).thenReturn("/malformed/url");

        // when
        final Mono<ErrorResponseDto> result = handler.handleMalformedUrlException(missingRequestValueException);

        // then
        StepVerifier.create(result)
                .assertNext(body -> {
                    assertThat(body.code()).isEqualTo(ApplicationErrorCodes.INVALID_URL.getCode());
                    assertThat(body.message()).contains("/malformed/url");
                })
                .verifyComplete();
    }

    @Test
    void handleArgumentMismatchExceptionTest() {
        // given
        final MethodArgumentTypeMismatchException methodArgumentTypeMismatchException = mock(MethodArgumentTypeMismatchException.class);
        when(methodArgumentTypeMismatchException.getMessage()).thenReturn("invalid type");

        // when
        final Mono<ErrorResponseDto> result = handler.handleArgumentMismatchException(methodArgumentTypeMismatchException);

        // then
        StepVerifier.create(result)
                .assertNext(body -> {
                    assertThat(body.code()).isEqualTo(ApplicationErrorCodes.MALFORMED_URL.getCode());
                    assertThat(body.message()).contains("invalid type");
                })
                .verifyComplete();
    }

    @Test
    void handleBugExceptionTest() {
        // given
        final Exception exception = new Exception("something went very wrong");

        // when
        final Mono<ErrorResponseDto> result = handler.handleBugException(exception);

        // then
        StepVerifier.create(result)
                .assertNext(body -> {
                    assertThat(body.code()).isEqualTo(GenericErrorCodes.GENERIC_ERROR_BUG.getCode());
                    assertThat(body.message()).contains("something went very wrong");
                })
                .verifyComplete();
    }

}