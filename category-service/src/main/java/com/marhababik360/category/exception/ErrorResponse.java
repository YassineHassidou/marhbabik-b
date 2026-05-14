package com.marhababik360.category.exception;

import java.time.Instant;

public class ErrorResponse {
    public String timestamp;
    public int status;
    public String error;
    public String message;
    public String path;
    public static ErrorResponse of(ApiException exception, String path) {
        ErrorResponse response = new ErrorResponse();
        response.timestamp = Instant.now().toString();
        response.status = exception.status();
        response.error = exception.error();
        response.message = exception.getMessage();
        response.path = path;
        return response;
    }
}
