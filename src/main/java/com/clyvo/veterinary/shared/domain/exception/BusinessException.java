package com.clyvo.veterinary.shared.domain.exception;

public class BusinessException extends RuntimeException {
    private final int statusCode;
    public BusinessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    public BusinessException(String message) {
        this(message, 400);
    }
    public int getStatusCode() { return statusCode; }
}
