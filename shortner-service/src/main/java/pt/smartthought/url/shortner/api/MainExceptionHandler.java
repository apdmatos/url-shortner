package pt.smartthought.url.shortner.api;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import pt.smartthought.url.shortner.api.dto.ErrorResponseDto;

import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice
@ResponseBody
@Slf4j
public class MainExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponseDto handleMessageNotReadable(HttpMessageNotReadableException ex) {
        ErrorResponseDto responseBody = ErrorResponseDto.INVALID_REQUEST;
        if (ex.getCause() instanceof JsonMappingException) {
            JsonMappingException cause = (JsonMappingException) ex.getCause();

            String invalidFields = cause.getPath().stream()
                    .map(c -> c.getFieldName())
                    .collect(Collectors.joining("."));

            responseBody = new ErrorResponseDto(String.format("invalid data %s", invalidFields));
        }
        return responseBody;
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponseDto handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ErrorResponseDto.INVALID_REQUEST;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponseDto handleArgumentNotValid(MethodArgumentNotValidException ex) {
        Set<String> missingErrorFields = ex.getBindingResult().getFieldErrors()
                .stream()
                .filter(error -> error.getRejectedValue() == null)
                .map(error -> error.getField()).collect(Collectors.toSet());
        Set<String> invalidErrorFields = ex.getBindingResult().getFieldErrors()
                .stream()
                .filter(error -> error.getRejectedValue() != null)
                .map(error -> error.getField()).collect(Collectors.toSet());

        String field = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .get();

        ErrorResponseDto responseBody = new ErrorResponseDto(String.format("invalid data %s", String.join(", ", invalidErrorFields)));
        if (!invalidErrorFields.isEmpty()) {
            responseBody = new ErrorResponseDto(String.format("invalid data %s", String.join(", ", invalidErrorFields)));
        }

        if (!missingErrorFields.isEmpty()) {
            String errorMessage = String.format("Missing mandatory attribute%s: %s", missingErrorFields.size() == 1 ? "" : "(s)", String.join(", ", missingErrorFields));
            responseBody = new ErrorResponseDto(errorMessage);
        }
        return responseBody;
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ErrorResponseDto handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return ErrorResponseDto.INVALID_MEDIA_TYPE;
    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ErrorResponseDto handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        return ErrorResponseDto.INVALID_ACCEPTS;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponseDto handleInternalError(Exception ex) {
        log.error("Unexpected generic error happened", ex);
        return ErrorResponseDto.UNEXPECTED_ERROR;
    }
}
