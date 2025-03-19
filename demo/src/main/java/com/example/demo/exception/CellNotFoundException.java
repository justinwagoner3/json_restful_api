package com.example.demo.exception;

public class CellNotFoundException extends RuntimeException {
    public CellNotFoundException(String message) {
        super(message);
    }
}
