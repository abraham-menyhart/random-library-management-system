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
    
    public BorrowerDto createBorrower(BorrowerDto borrowerDto) {
        log.debug("Creating new borrower with email: {}", borrowerDto.getEmail());
        
        validateEmailUniqueness(borrowerDto.getEmail());
        
        Borrower borrower = borrowerMapper.toEntity(borrowerDto);
        Borrower savedBorrower = borrowerRepository.save(borrower);
        
        log.info("Borrower created successfully with ID: {}", savedBorrower.getId());
        return borrowerMapper.toDto(savedBorrower);
    }
    
    public BorrowerDto getBorrower(Long id) {
        log.debug("Fetching borrower with ID: {}", id);
        
        Borrower borrower = findBorrowerOrThrow(id);
        
        return borrowerMapper.toDto(borrower);
    }
    
    public List<BookDto> getBorrowedBooks(Long borrowerId) {
        log.debug("Fetching books borrowed by borrower ID: {}", borrowerId);
        
        validateBorrowerExists(borrowerId);
        
        List<Book> borrowedBooks = bookRepository.findByBorrowerId(borrowerId);
        return mapBooksToDto(borrowedBooks);
    }
    
    public List<BorrowerDto> getAllBorrowers() {
        log.debug("Fetching all borrowers");
        
        List<Borrower> borrowers = borrowerRepository.findAll();
        return mapBorrowersToDto(borrowers);
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