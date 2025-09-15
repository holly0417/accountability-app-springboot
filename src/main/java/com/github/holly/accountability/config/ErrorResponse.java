package com.github.holly.accountability.config;

import java.util.Map;

public class ErrorResponse extends GenericResponse {

    private Map<String, String> errors;

    public ErrorResponse(String message) {
        super(message);
    }

    public ErrorResponse(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

}