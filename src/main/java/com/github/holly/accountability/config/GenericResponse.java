package com.github.holly.accountability.config;


public class GenericResponse {
    private String message;

    public GenericResponse(String message) {
        super();
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
