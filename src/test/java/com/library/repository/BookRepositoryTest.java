package com.library.repository;

import com.library.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void findByAvailable_shouldReturnAvailableBooks_whenAvailableIsTrue() {
        //given
        Book availableBook = new Book("Available Book", "Test Author", "1234567890");
        Book unavailableBook = new Book("Unavailable Book", "Test Author", "0987654321");
        unavailableBook.setBorrowerId(1L);
        
        bookRepository.save(availableBook);
        bookRepository.save(unavailableBook);
        
        //when
        List<Book> availableBooks = bookRepository.findByAvailable(true);
        
        //then
        assertThat(availableBooks).hasSize(1);
        assertThat(availableBooks.get(0).getTitle()).isEqualTo("Available Book");
        assertThat(availableBooks.get(0).getAvailable()).isTrue();
    }

    @Test
    void findByBorrowerId_shouldReturnBorrowedBooks_whenBorrowerIdProvided() {
        //given
        Long borrowerId = 1L;
        Book borrowedBook = new Book("Borrowed Book", "Test Author", "1111111111");
        borrowedBook.setBorrowerId(borrowerId);
        
        Book availableBook = new Book("Available Book", "Test Author", "2222222222");
        
        bookRepository.save(borrowedBook);
        bookRepository.save(availableBook);
        
        //when
        List<Book> borrowedBooks = bookRepository.findByBorrowerId(borrowerId);
        
        //then
        assertThat(borrowedBooks).hasSize(1);
        assertThat(borrowedBooks.get(0).getTitle()).isEqualTo("Borrowed Book");
        assertThat(borrowedBooks.get(0).getBorrowerId()).isEqualTo(borrowerId);
        assertThat(borrowedBooks.get(0).getAvailable()).isFalse();
    }

    @Test
    void findByIsbn_shouldReturnBook_whenIsbnExists() {
        //given
        String isbn = "9780123456789";
        Book book = new Book("Test Book", "Test Author", isbn);
        bookRepository.save(book);
        
        //when
        Optional<Book> foundBook = bookRepository.findByIsbn(isbn);
        
        //then
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getIsbn()).isEqualTo(isbn);
        assertThat(foundBook.get().getTitle()).isEqualTo("Test Book");
    }

    @Test
    void findByIsbn_shouldReturnEmpty_whenIsbnDoesNotExist() {
        //given
        String nonExistentIsbn = "9999999999999";
        
        //when
        Optional<Book> foundBook = bookRepository.findByIsbn(nonExistentIsbn);
        
        //then
        assertThat(foundBook).isEmpty();
    }

    @Test
    void countByAvailable_shouldReturnCorrectCount_whenCountingAvailableBooks() {
        //given
        Book availableBook1 = new Book("Book 1", "Author 1", "1111111111");
        Book availableBook2 = new Book("Book 2", "Author 2", "2222222222");
        Book borrowedBook = new Book("Book 3", "Author 3", "3333333333");
        borrowedBook.setBorrowerId(1L);
        
        bookRepository.save(availableBook1);
        bookRepository.save(availableBook2);
        bookRepository.save(borrowedBook);
        
        //when
        long availableCount = bookRepository.countByAvailable(true);
        long borrowedCount = bookRepository.countByAvailable(false);
        
        //then
        assertThat(availableCount).isEqualTo(2);
        assertThat(borrowedCount).isEqualTo(1);
    }
}