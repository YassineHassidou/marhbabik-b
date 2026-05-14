package com.marhababik360.category.exception;

public class ApiException extends RuntimeException {
    private final int status;
    private final String error;
    public ApiException(int status, String error, String message) { super(message); this.status = status; this.error = error; }
    public int status() { return status; }
    public String error() { return error; }
}
