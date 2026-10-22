package com.clyvo.veterinary.exceptions;

public class BusinessException extends DomainException {
    public BusinessException(String message) {
        super(message);
    }
}
