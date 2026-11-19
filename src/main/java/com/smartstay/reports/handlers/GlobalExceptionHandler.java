package com.smartstay.reports.handlers;

import com.smartstay.reports.exceptions.BadRequestException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import tools.jackson.databind.exc.InvalidFormatException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodValidationException(MethodArgumentNotValidException manv, WebRequest request) {
        String errorMessage = manv.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation failed");
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleInvalidFormat(HttpMessageNotReadableException ex) {

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormat) {

            String field = invalidFormat.getPath().isEmpty()
                    ? "request"
                    : invalidFormat.getPath().getFirst().getPropertyName();

            return new ResponseEntity<>(field + " has invalid format", HttpStatus.BAD_REQUEST);
        }

        //return new ResponseEntity<>("Invalid request body : " + ex.getMessage() , HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>("Invalid request body", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
