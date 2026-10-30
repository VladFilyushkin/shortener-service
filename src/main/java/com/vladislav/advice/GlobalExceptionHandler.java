package com.vladislav.advice;

import com.vladislav.exception.LinkExpiredException;
import com.vladislav.exception.LinkNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LinkNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleLinkNotFoundException(LinkNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(LinkExpiredException.class)
    @ResponseStatus(HttpStatus.GONE)
    public String handleLinkExpiredException(LinkExpiredException e) {
        return e.getMessage();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> exceptions = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            exceptions.put(error.getField(), error.getDefaultMessage());
        }
        return exceptions;
    }
}
