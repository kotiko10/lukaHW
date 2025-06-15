package com.example.exceptions;

public class LoadDatabasePropertiesException extends RuntimeException {
  public LoadDatabasePropertiesException(String message, Exception e) {
    super(message, e);
  }
}