package com.library.exception;

public class BookNotFoundException extends RuntimeException {
    
    public BookNotFoundException(String message) {
        super(message);
    }
    
    public BookNotFoundException(Long bookId) {
        super("Book not found with ID: " + bookId);
    }
}