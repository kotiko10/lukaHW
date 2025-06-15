package com.example.exceptions;

public class DatabaseWriteException extends RuntimeException {
    public DatabaseWriteException(String message, Exception e) {
        super(message, e);
    }
}
