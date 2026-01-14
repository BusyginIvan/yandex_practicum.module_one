package ru.yandex.practicum.api;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String expected = ex.getRequiredType() == null ? "unknown" : ex.getRequiredType().getSimpleName();
        Object value = ex.getValue();

        String message = "Invalid value for '" + name + "': " + value + " (expected " + expected + ")";
        return buildBadRequest(ex, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        return buildBadRequest(ex, "Malformed JSON request body");
    }

    @ExceptionHandler({
        MissingServletRequestPartException.class,
        MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleMissingRequestPart(Exception ex) {
        return buildBadRequest(ex, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAnyException(
        Exception ex,
        HttpServletResponse response
    ) {
        return build(ex, resolveStatus(ex, response), ex.getMessage());
    }

    private static ResponseEntity<ErrorResponse> buildBadRequest(Exception ex, String message) {
        return build(ex, HttpStatus.BAD_REQUEST.value(), message);
    }

    private static ResponseEntity<ErrorResponse> build(Exception ex, int status, String message) {
        ErrorResponse body = new ErrorResponse(ex.getClass().getSimpleName(), message);
        return ResponseEntity.status(status).body(body);
    }

    private static int resolveStatus(Exception ex, HttpServletResponse response) {
        int current = response.getStatus();
        if (current != HttpStatus.OK.value()) return current;

        ResponseStatus rs = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        if (rs != null) return rs.code().value();

        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    public record ErrorResponse(String name, String message) { }
}
