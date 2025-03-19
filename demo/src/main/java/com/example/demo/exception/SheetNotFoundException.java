package com.example.demo.exception;

public class SheetNotFoundException extends RuntimeException {
    public SheetNotFoundException(String message) {
        super(message);
    }
}
