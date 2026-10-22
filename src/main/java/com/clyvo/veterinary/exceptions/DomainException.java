package com.clyvo.veterinary.exceptions;

/**
 * Base de todas as exceções de regra de negócio da plataforma.
 * Permite que a camada Web trate erros de domínio de forma única,
 * sem capturar exceções genéricas (Exception / RuntimeException).
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
