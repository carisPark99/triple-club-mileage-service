package com.triple.exception;

import com.triple.enums.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler({ReviewServiceException.class})
    public ResponseEntity<Object> handleRestApiException(final ReviewServiceException exception) {
        log.warn("ReviewServiceException occur: ", exception);
        return this.makeErrorResponseEntity(String.valueOf(exception.getErrorCode()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleException(final Exception exception) {
        log.warn("Exception occur: ", exception);
        return this.makeErrorResponseEntity(String.valueOf(ErrorCode.UNKNOWN_EXCEPTION));
    }

    private ResponseEntity<ErrorResponse> makeErrorResponseEntity(final ErrorCode errorResult) {
        return ResponseEntity.status(errorResult.getErrorStatus())
                .body(new ErrorResponse(errorResult.name(), errorResult.getErrorMessage(), errorResult.getErrorCode()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request) {

        final List<String> errorList = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        log.info("Invalid DTO Parameter errors : {}", errorList);
        return this.makeErrorResponseEntity(errorList.toString());
    }

    private ResponseEntity<Object> makeErrorResponseEntity(final String errorDescription) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.toString(), errorDescription, ErrorCode.REVIRE_SERVICE_BAD_REQUEST.getErrorCode()));
    }

    @Getter
    static class ErrorResponse {
        private final String statusCode;
        private final String errorMessage;
        private final String errorCode;

        public ErrorResponse(String statusCode, String errorMessage, String errorCode) {
            this.statusCode = statusCode;
            this.errorMessage = errorMessage;
            this.errorCode = errorCode;
        }
    }

}
