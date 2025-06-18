package com.library.service;

import com.library.dto.BookDto;
import com.library.entity.Book;
import com.library.exception.BookAlreadyBorrowedException;
import com.library.exception.BookNotFoundException;
import com.library.exception.BorrowerNotFoundException;
import com.library.mapper.BookMapper;
import com.library.repository.BookRepository;
import com.library.repository.BorrowerRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.annotation.Observed;
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
public class BookService {
    
    private final BookRepository bookRepository;
    private final BorrowerRepository borrowerRepository;
    private final BookMapper bookMapper;
    private final Counter booksBorrowedCounter;
    private final Counter booksAddedCounter;
    private final Timer bookOperationTimer;
    
    public List<BookDto> getAllBooks() {
        Timer.Sample sample = Timer.start();
        try {
            log.debug("Fetching all books");
            List<Book> books = bookRepository.findAll();
            return mapBooksToDto(books);
        } finally {
            sample.stop(bookOperationTimer);
        }
    }
    
    public BookDto addBook(BookDto bookDto) {
        Timer.Sample sample = Timer.start();
        try {
            log.debug("Adding new book: {}", bookDto.getTitle());
            Book book = bookMapper.toEntity(bookDto);
            Book savedBook = bookRepository.save(book);
            booksAddedCounter.increment();
            log.info("Book added successfully with ID: {}", savedBook.getId());
            return bookMapper.toDto(savedBook);
        } finally {
            sample.stop(bookOperationTimer);
        }
    }

    @Observed(name = "book.borrow", contextualName = "borrowing-book")
    public BookDto borrowBook(Long bookId, Long borrowerId) {
        Timer.Sample sample = Timer.start();
        try {
            log.debug("Processing borrow request for book ID: {} by borrower ID: {}", bookId, borrowerId);
            
            Book book = findBookOrThrow(bookId);
            validateBookAvailability(book);
            validateBorrowerExists(borrowerId);
            
            Book borrowedBook = updateBookBorrower(book, borrowerId);
            booksBorrowedCounter.increment();
            
            log.info("Book ID: {} successfully borrowed by borrower ID: {}", bookId, borrowerId);
            return bookMapper.toDto(borrowedBook);
        } finally {
            sample.stop(bookOperationTimer);
        }
    }
    
    private List<BookDto> mapBooksToDto(List<Book> books) {
        return books.stream()
                .map(bookMapper::toDto)
                .collect(toList());
    }
    
    private Book findBookOrThrow(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }
    
    private void validateBookAvailability(Book book) {
        if (!book.getAvailable()) {
            log.warn("Book ID: {} is already borrowed", book.getId());
            throw new BookAlreadyBorrowedException(book.getId());
        }
    }
    
    private void validateBorrowerExists(Long borrowerId) {
        borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new BorrowerNotFoundException(borrowerId));
    }
    
    private Book updateBookBorrower(Book book, Long borrowerId) {
        book.setBorrowerId(borrowerId);
        return bookRepository.save(book);
    }
}