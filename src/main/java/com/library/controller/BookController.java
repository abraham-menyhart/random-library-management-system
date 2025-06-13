package com.library.controller;

import com.library.dto.BookDto;
import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {
    
    private final BookService bookService;
    
    @GetMapping
    public ResponseEntity<List<BookDto>> getAllBooks() {
        log.debug("GET /api/books - Fetching all books");
        List<BookDto> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }
    
    @PostMapping
    public ResponseEntity<BookDto> addBook(@Valid @RequestBody BookDto bookDto) {
        log.debug("POST /api/books - Adding new book: {}", bookDto.getTitle());
        BookDto savedBook = bookService.addBook(bookDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }
    
    @PostMapping("/{bookId}/borrow/{borrowerId}")
    public ResponseEntity<BookDto> borrowBook(@PathVariable Long bookId, @PathVariable Long borrowerId) {
        log.debug("POST /api/books/{}/borrow/{} - Borrowing book", bookId, borrowerId);
        BookDto borrowedBook = bookService.borrowBook(bookId, borrowerId);
        return ResponseEntity.ok(borrowedBook);
    }
}