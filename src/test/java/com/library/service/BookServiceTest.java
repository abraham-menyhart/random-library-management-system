package com.library.service;

import com.library.dto.BookDto;
import com.library.entity.Book;
import com.library.entity.Borrower;
import com.library.exception.BookAlreadyBorrowedException;
import com.library.exception.BookNotFoundException;
import com.library.exception.BorrowerNotFoundException;
import com.library.mapper.BookMapper;
import com.library.repository.BookRepository;
import com.library.repository.BorrowerRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private Counter booksBorrowedCounter;

    @Mock
    private Counter booksAddedCounter;

    @Mock
    private Timer bookOperationTimer;

    @InjectMocks
    private BookService bookService;


    @Test
    void borrowBook_shouldUpdateBookAndReturnDto_whenBookIsAvailable() {
        //given
        Long bookId = 1L;
        Long borrowerId = 2L;
        Book availableBook = new Book("Test Book", "Test Author", "ISBN123");
        availableBook.setId(bookId);
        availableBook.setAvailable(true);

        Borrower borrower = new Borrower("John Doe", "john@example.com");
        borrower.setId(borrowerId);

        Book borrowedBook = new Book("Test Book", "Test Author", "ISBN123");
        borrowedBook.setId(bookId);
        borrowedBook.setBorrowerId(borrowerId);

        BookDto borrowedBookDto = new BookDto(bookId, "Test Book", "Test Author", "ISBN123", false, borrowerId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(availableBook));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(bookRepository.save(any(Book.class))).thenReturn(borrowedBook);
        when(bookMapper.toDto(borrowedBook)).thenReturn(borrowedBookDto);

        //when
        BookDto result = bookService.borrowBook(bookId, borrowerId);

        //then
        assertThat(result.getId()).isEqualTo(bookId);
        assertThat(result.getBorrowerId()).isEqualTo(borrowerId);
        assertThat(result.getAvailable()).isFalse();
        verify(bookRepository).findById(bookId);
        verify(borrowerRepository).findById(borrowerId);
        verify(bookRepository).save(any(Book.class));
        verify(bookMapper).toDto(borrowedBook);
    }

    @Test
    void borrowBook_shouldThrowBookNotFoundException_whenBookDoesNotExist() {
        //given
        Long nonExistentBookId = 999L;
        Long borrowerId = 1L;
        when(bookRepository.findById(nonExistentBookId)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> bookService.borrowBook(nonExistentBookId, borrowerId))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book not found with ID: " + nonExistentBookId);

        verify(bookRepository).findById(nonExistentBookId);
        verify(borrowerRepository, never()).findById(any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void borrowBook_shouldThrowBookAlreadyBorrowedException_whenBookIsAlreadyBorrowed() {
        //given
        Long bookId = 1L;
        Long borrowerId = 2L;
        Book borrowedBook = new Book("Test Book", "Test Author", "ISBN123");
        borrowedBook.setId(bookId);
        borrowedBook.setBorrowerId(3L); // Already borrowed by someone else
        borrowedBook.setAvailable(false);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(borrowedBook));

        //when & then
        assertThatThrownBy(() -> bookService.borrowBook(bookId, borrowerId))
                .isInstanceOf(BookAlreadyBorrowedException.class)
                .hasMessage("Book with ID " + bookId + " is already borrowed");

        verify(bookRepository).findById(bookId);
        verify(borrowerRepository, never()).findById(any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void borrowBook_shouldThrowBorrowerNotFoundException_whenBorrowerDoesNotExist() {
        //given
        Long bookId = 1L;
        Long nonExistentBorrowerId = 999L;
        Book availableBook = new Book("Test Book", "Test Author", "ISBN123");
        availableBook.setId(bookId);
        availableBook.setAvailable(true);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(availableBook));
        when(borrowerRepository.findById(nonExistentBorrowerId)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> bookService.borrowBook(bookId, nonExistentBorrowerId))
                .isInstanceOf(BorrowerNotFoundException.class)
                .hasMessage("Borrower not found with ID: " + nonExistentBorrowerId);

        verify(bookRepository).findById(bookId);
        verify(borrowerRepository).findById(nonExistentBorrowerId);
        verify(bookRepository, never()).save(any());
    }

}