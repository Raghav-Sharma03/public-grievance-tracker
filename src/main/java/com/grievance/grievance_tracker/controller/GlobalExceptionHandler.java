package com.grievance.grievance_tracker.controller;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 500 - Unexpected runtime errors
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(RuntimeException ex, Model model) {
        log.error("Unexpected runtime error occurred: {}", ex.getMessage());
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "Something went wrong. Please try again later.");
        return "error";
    }

    // 404 - Resource not found
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorMessage", "The requested resource was not found.");
        return "error";
    }

    // 400 - Invalid input
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex, Model model) {
        log.warn("Invalid request: {}", ex.getMessage());
        model.addAttribute("errorCode", "400");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    // 403 - Security violation
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(SecurityException ex, Model model) {
        log.warn("Security violation attempt detected.");
        model.addAttribute("errorCode", "403");
        model.addAttribute("errorMessage", "You are not allowed to perform this action.");
        return "error";
    }

    // 500 - Unexpected errors
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Unexpected error occurred: {}", ex.getMessage());
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorMessage", "Something went wrong. Please try again later.");
        return "error";
    }
}
