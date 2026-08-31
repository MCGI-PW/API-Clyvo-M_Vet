package com.clyvo.veterinary.shared.domain.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " com id '" + id + "' não encontrado.", 404);
    }
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}
