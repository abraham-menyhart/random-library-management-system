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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
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

    @InjectMocks
    private BookService bookService;

    @Test
    void getAllBooks_shouldReturnAllBooks_whenBooksExist() {
        //given
        Book book1 = new Book("Book 1", "Author 1", "ISBN1");
        book1.setId(1L);
        Book book2 = new Book("Book 2", "Author 2", "ISBN2");
        book2.setId(2L);
        
        BookDto bookDto1 = new BookDto(1L, "Book 1", "Author 1", "ISBN1", true, null);
        BookDto bookDto2 = new BookDto(2L, "Book 2", "Author 2", "ISBN2", true, null);
        
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));
        when(bookMapper.toDto(book1)).thenReturn(bookDto1);
        when(bookMapper.toDto(book2)).thenReturn(bookDto2);

        //when
        List<BookDto> result = bookService.getAllBooks();

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Book 1");
        assertThat(result.get(1).getTitle()).isEqualTo("Book 2");
        verify(bookRepository).findAll();
        verify(bookMapper).toDto(book1);
        verify(bookMapper).toDto(book2);
    }

    @Test
    void addBook_shouldSaveAndReturnBook_whenValidBookProvided() {
        //given
        BookDto bookDto = new BookDto("New Book", "New Author", "ISBN123");
        Book book = new Book("New Book", "New Author", "ISBN123");
        Book savedBook = new Book("New Book", "New Author", "ISBN123");
        savedBook.setId(1L);
        BookDto savedBookDto = new BookDto(1L, "New Book", "New Author", "ISBN123", true, null);
        
        when(bookMapper.toEntity(bookDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(savedBook);
        when(bookMapper.toDto(savedBook)).thenReturn(savedBookDto);

        //when
        BookDto result = bookService.addBook(bookDto);

        //then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("New Book");
        assertThat(result.getAuthor()).isEqualTo("New Author");
        assertThat(result.getIsbn()).isEqualTo("ISBN123");
        assertThat(result.getAvailable()).isTrue();
        verify(bookMapper).toEntity(bookDto);
        verify(bookRepository).save(book);
        verify(bookMapper).toDto(savedBook);
    }

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