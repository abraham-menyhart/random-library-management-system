package com.library.service;

import com.library.dto.BookDto;
import com.library.dto.BorrowerDto;
import com.library.entity.Book;
import com.library.entity.Borrower;
import com.library.exception.BorrowerNotFoundException;
import com.library.exception.DuplicateEmailException;
import com.library.mapper.BookMapper;
import com.library.mapper.BorrowerMapper;
import com.library.repository.BookRepository;
import com.library.repository.BorrowerRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BorrowerService {
    
    private final BorrowerRepository borrowerRepository;
    private final BookRepository bookRepository;
    private final BorrowerMapper borrowerMapper;
    private final BookMapper bookMapper;
    private final Counter borrowersCreatedCounter;
    private final Timer borrowerOperationTimer;
    
    public BorrowerDto createBorrower(BorrowerDto borrowerDto) {
        Timer.Sample sample = Timer.start();
        try {
            log.debug("Creating new borrower with email: {}", borrowerDto.getEmail());
            
            validateEmailUniqueness(borrowerDto.getEmail());
            
            Borrower borrower = borrowerMapper.toEntity(borrowerDto);
            Borrower savedBorrower = borrowerRepository.save(borrower);
            borrowersCreatedCounter.increment();
            
            log.info("Borrower created successfully with ID: {}", savedBorrower.getId());
            return borrowerMapper.toDto(savedBorrower);
        } finally {
            sample.stop(borrowerOperationTimer);
        }
    }
    
    public BorrowerDto getBorrower(Long id) {
        Timer.Sample sample = Timer.start();
        try {
            log.debug("Fetching borrower with ID: {}", id);
            
            Borrower borrower = findBorrowerOrThrow(id);
            
            return borrowerMapper.toDto(borrower);
        } finally {
            sample.stop(borrowerOperationTimer);
        }
    }
    
    public List<BookDto> getBorrowedBooks(Long borrowerId) {
        Timer.Sample sample = Timer.start();
        try {
            log.debug("Fetching books borrowed by borrower ID: {}", borrowerId);
            
            validateBorrowerExists(borrowerId);
            
            List<Book> borrowedBooks = bookRepository.findByBorrowerId(borrowerId);
            return mapBooksToDto(borrowedBooks);
        } finally {
            sample.stop(borrowerOperationTimer);
        }
    }
    
    public List<BorrowerDto> getAllBorrowers() {
        Timer.Sample sample = Timer.start();
        try {
            log.debug("Fetching all borrowers");
            
            List<Borrower> borrowers = borrowerRepository.findAll();
            return mapBorrowersToDto(borrowers);
        } finally {
            sample.stop(borrowerOperationTimer);
        }
    }
    
    private void validateEmailUniqueness(String email) {
        if (borrowerRepository.existsByEmail(email)) {
            log.warn("Attempt to create borrower with duplicate email: {}", email);
            throw new DuplicateEmailException("Email already exists: " + email);
        }
    }
    
    private Borrower findBorrowerOrThrow(Long borrowerId) {
        return borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new BorrowerNotFoundException(borrowerId));
    }
    
    private void validateBorrowerExists(Long borrowerId) {
        borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new BorrowerNotFoundException(borrowerId));
    }
    
    private List<BorrowerDto> mapBorrowersToDto(List<Borrower> borrowers) {
        return borrowers.stream()
                .map(borrowerMapper::toDto)
                .collect(toList());
    }
    
    private List<BookDto> mapBooksToDto(List<Book> books) {
        return books.stream()
                .map(bookMapper::toDto)
                .collect(toList());
    }
}