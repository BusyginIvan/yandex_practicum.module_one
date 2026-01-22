package ru.yandex.practicum.api;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        String message = formatBindingErrors(
            ex.getBindingResult().getFieldErrors(),
            ex.getBindingResult().getGlobalErrors()
        );
        return buildBadRequest(ex, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
            .map(ExceptionHandlerAdvice::formatViolation)
            .toList();

        String message = details.isEmpty()
            ? "Validation failed"
            : "Validation failed: " + String.join("; ", details);

        return buildBadRequest(ex, message);
    }

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

    private static String formatBindingErrors(List<FieldError> fieldErrors, List<ObjectError> globalErrors) {
        List<String> details = new ArrayList<>();

        for (FieldError fe : fieldErrors) {
            String msg = safeDefaultMessage(fe);
            details.add(fe.getField() + " - " + msg);
        }

        for (ObjectError oe : globalErrors) {
            String msg = safeDefaultMessage(oe);
            details.add(oe.getObjectName() + " - " + msg);
        }

        return details.isEmpty()
            ? "Validation failed"
            : "Validation failed: " + String.join("; ", details);
    }

    private static String formatViolation(ConstraintViolation<?> v) {
        String path = v.getPropertyPath() == null ? "param" : v.getPropertyPath().toString();
        String leaf = lastPathSegment(path);
        String msg = (v.getMessage() == null || v.getMessage().isBlank()) ? "invalid" : v.getMessage();
        return leaf + " - " + msg;
    }

    private static String lastPathSegment(String path) {
        int dot = path.lastIndexOf('.');
        return (dot >= 0 && dot + 1 < path.length()) ? path.substring(dot + 1) : path;
    }

    private static String safeDefaultMessage(ObjectError error) {
        String msg = error.getDefaultMessage();
        if (msg != null && !msg.isBlank()) return msg;
        return "invalid";
    }

    public record ErrorResponse(String name, String message) { }
}
