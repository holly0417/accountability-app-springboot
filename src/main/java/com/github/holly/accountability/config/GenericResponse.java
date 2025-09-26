package com.github.holly.accountability.config;

public class GenericResponse {
    private String message;
    private String error;

    public GenericResponse(String message) {
        super();
        this.message = message;
    }

    public GenericResponse(String message, String error) {
        super();
        this.message = message;
        this.error = error;
    }

    public String getError() {
        return this.error;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setError(String error) {
        this.error = error;
    }

}
