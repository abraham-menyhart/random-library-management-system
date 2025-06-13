package com.library.exception;

public class BookAlreadyBorrowedException extends RuntimeException {
    
    public BookAlreadyBorrowedException(String message) {
        super(message);
    }
    
    public BookAlreadyBorrowedException(Long bookId) {
        super("Book with ID " + bookId + " is already borrowed");
    }
}