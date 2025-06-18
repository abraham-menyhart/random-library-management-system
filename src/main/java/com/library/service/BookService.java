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
    private final Counter successfulBorrowsCounter;
    private final Counter failedBorrowsCounter;
    
    public List<BookDto> getAllBooks() {
        log.debug("Fetching all books");
        List<Book> books = bookRepository.findAll();
        return mapBooksToDto(books);
    }
    
    public BookDto addBook(BookDto bookDto) {
        log.debug("Adding new book: {}", bookDto.getTitle());
        Book book = bookMapper.toEntity(bookDto);
        Book savedBook = bookRepository.save(book);
        log.info("Book added successfully with ID: {}", savedBook.getId());
        return bookMapper.toDto(savedBook);
    }

    @Observed(name = "book.borrow", contextualName = "borrowing-book")
    public BookDto borrowBook(Long bookId, Long borrowerId) {
        try {
            log.debug("Processing borrow request for book ID: {} by borrower ID: {}", bookId, borrowerId);
            
            Book book = findBookOrThrow(bookId);
            validateBookAvailability(book);
            validateBorrowerExists(borrowerId);
            
            Book borrowedBook = updateBookBorrower(book, borrowerId);
            successfulBorrowsCounter.increment();
            
            log.info("Book ID: {} successfully borrowed by borrower ID: {}", bookId, borrowerId);
            return bookMapper.toDto(borrowedBook);
        } catch (Exception e) {
            failedBorrowsCounter.increment();
            throw e;
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