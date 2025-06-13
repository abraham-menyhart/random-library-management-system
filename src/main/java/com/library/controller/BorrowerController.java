package com.library.controller;

import com.library.dto.BookDto;
import com.library.dto.BorrowerDto;
import com.library.service.BorrowerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/borrowers")
@RequiredArgsConstructor
@Slf4j
public class BorrowerController {
    
    private final BorrowerService borrowerService;
    
    @GetMapping
    public ResponseEntity<List<BorrowerDto>> getAllBorrowers() {
        log.debug("GET /api/borrowers - Fetching all borrowers");
        List<BorrowerDto> borrowers = borrowerService.getAllBorrowers();
        return ResponseEntity.ok(borrowers);
    }
    
    @PostMapping
    public ResponseEntity<BorrowerDto> createBorrower(@Valid @RequestBody BorrowerDto borrowerDto) {
        log.debug("POST /api/borrowers - Creating new borrower: {}", borrowerDto.getEmail());
        BorrowerDto savedBorrower = borrowerService.createBorrower(borrowerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBorrower);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BorrowerDto> getBorrower(@PathVariable Long id) {
        log.debug("GET /api/borrowers/{} - Fetching borrower", id);
        BorrowerDto borrower = borrowerService.getBorrower(id);
        return ResponseEntity.ok(borrower);
    }
    
    @GetMapping("/{id}/books")
    public ResponseEntity<List<BookDto>> getBorrowedBooks(@PathVariable Long id) {
        log.debug("GET /api/borrowers/{}/books - Fetching borrowed books", id);
        List<BookDto> borrowedBooks = borrowerService.getBorrowedBooks(id);
        return ResponseEntity.ok(borrowedBooks);
    }
}