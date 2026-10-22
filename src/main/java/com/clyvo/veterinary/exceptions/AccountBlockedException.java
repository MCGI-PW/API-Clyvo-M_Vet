package com.clyvo.veterinary.exceptions;

public class AccountBlockedException extends DomainException {
    public AccountBlockedException(String message) {
        super(message);
    }
}
