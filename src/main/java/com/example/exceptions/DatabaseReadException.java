package com.example.exceptions;

public class DatabaseReadException extends RuntimeException {
    public DatabaseReadException(String message, Exception e) {
        super(message, e);
    }
}
