package com.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @NotBlank(message = "Title is required")
    private String title;
    
    @Column(nullable = false)
    @NotBlank(message = "Author is required")
    private String author;
    
    @Column(unique = true)
    private String isbn;
    
    @Column(nullable = false)
    private Boolean available = true;
    
    @Column(name = "borrowed_by")
    private Long borrowerId;
    
    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = true;
    }
    
    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
        this.available = (borrowerId == null);
    }
}