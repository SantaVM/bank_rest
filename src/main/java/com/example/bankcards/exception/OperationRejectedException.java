package com.example.bankcards.exception;

public class OperationRejectedException extends RuntimeException {
    public OperationRejectedException(String message) {
        super(message);
    }
}
