package com.library.exception;

public class BorrowerNotFoundException extends RuntimeException {
    
    public BorrowerNotFoundException(String message) {
        super(message);
    }
    
    public BorrowerNotFoundException(Long borrowerId) {
        super("Borrower not found with ID: " + borrowerId);
    }
}