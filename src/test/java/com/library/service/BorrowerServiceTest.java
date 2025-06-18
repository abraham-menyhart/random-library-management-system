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
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowerMapper borrowerMapper;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private Counter borrowersCreatedCounter;

    @Mock
    private Timer borrowerOperationTimer;

    @InjectMocks
    private BorrowerService borrowerService;

    @Test
    void createBorrower_shouldSaveAndReturnBorrower_whenValidBorrowerProvided() {
        //given
        BorrowerDto borrowerDto = new BorrowerDto("John Doe", "john@example.com");
        Borrower borrower = new Borrower("John Doe", "john@example.com");
        Borrower savedBorrower = new Borrower("John Doe", "john@example.com");
        savedBorrower.setId(1L);
        BorrowerDto savedBorrowerDto = new BorrowerDto(1L, "John Doe", "john@example.com");
        
        when(borrowerRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(borrowerMapper.toEntity(borrowerDto)).thenReturn(borrower);
        when(borrowerRepository.save(borrower)).thenReturn(savedBorrower);
        when(borrowerMapper.toDto(savedBorrower)).thenReturn(savedBorrowerDto);

        //when
        BorrowerDto result = borrowerService.createBorrower(borrowerDto);

        //then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(borrowerRepository).existsByEmail("john@example.com");
        verify(borrowerMapper).toEntity(borrowerDto);
        verify(borrowerRepository).save(borrower);
        verify(borrowerMapper).toDto(savedBorrower);
    }

    @Test
    void createBorrower_shouldThrowDuplicateEmailException_whenEmailAlreadyExists() {
        //given
        BorrowerDto borrowerDto = new BorrowerDto("John Doe", "existing@example.com");
        when(borrowerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        //when & then
        assertThatThrownBy(() -> borrowerService.createBorrower(borrowerDto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email already exists: existing@example.com");
        
        verify(borrowerRepository).existsByEmail("existing@example.com");
        verify(borrowerRepository, never()).save(any());
    }


    @Test
    void getBorrower_shouldThrowBorrowerNotFoundException_whenBorrowerDoesNotExist() {
        //given
        Long nonExistentBorrowerId = 999L;
        when(borrowerRepository.findById(nonExistentBorrowerId)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> borrowerService.getBorrower(nonExistentBorrowerId))
                .isInstanceOf(BorrowerNotFoundException.class)
                .hasMessage("Borrower not found with ID: " + nonExistentBorrowerId);
        
        verify(borrowerRepository).findById(nonExistentBorrowerId);
    }

    @Test
    void getBorrowedBooks_shouldReturnBorrowedBooks_whenBorrowerExists() {
        //given
        Long borrowerId = 1L;
        Borrower borrower = new Borrower("John Doe", "john@example.com");
        borrower.setId(borrowerId);
        
        Book book1 = new Book("Book 1", "Author 1", "ISBN1");
        book1.setId(1L);
        book1.setBorrowerId(borrowerId);
        
        Book book2 = new Book("Book 2", "Author 2", "ISBN2");
        book2.setId(2L);
        book2.setBorrowerId(borrowerId);
        
        BookDto bookDto1 = new BookDto(1L, "Book 1", "Author 1", "ISBN1", false, borrowerId);
        BookDto bookDto2 = new BookDto(2L, "Book 2", "Author 2", "ISBN2", false, borrowerId);
        
        when(borrowerRepository.findById(borrowerId)).thenReturn(Optional.of(borrower));
        when(bookRepository.findByBorrowerId(borrowerId)).thenReturn(Arrays.asList(book1, book2));
        when(bookMapper.toDto(book1)).thenReturn(bookDto1);
        when(bookMapper.toDto(book2)).thenReturn(bookDto2);

        //when
        List<BookDto> result = borrowerService.getBorrowedBooks(borrowerId);

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBorrowerId()).isEqualTo(borrowerId);
        assertThat(result.get(1).getBorrowerId()).isEqualTo(borrowerId);
        assertThat(result.get(0).getTitle()).isEqualTo("Book 1");
        assertThat(result.get(1).getTitle()).isEqualTo("Book 2");
        verify(borrowerRepository).findById(borrowerId);
        verify(bookRepository).findByBorrowerId(borrowerId);
        verify(bookMapper).toDto(book1);
        verify(bookMapper).toDto(book2);
    }

    @Test
    void getBorrowedBooks_shouldThrowBorrowerNotFoundException_whenBorrowerDoesNotExist() {
        //given
        Long nonExistentBorrowerId = 999L;
        when(borrowerRepository.findById(nonExistentBorrowerId)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> borrowerService.getBorrowedBooks(nonExistentBorrowerId))
                .isInstanceOf(BorrowerNotFoundException.class)
                .hasMessage("Borrower not found with ID: " + nonExistentBorrowerId);
        
        verify(borrowerRepository).findById(nonExistentBorrowerId);
        verify(bookRepository, never()).findByBorrowerId(any());
    }
}