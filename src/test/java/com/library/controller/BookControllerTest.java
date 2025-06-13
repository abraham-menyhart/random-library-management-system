package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.BookDto;
import com.library.exception.BookAlreadyBorrowedException;
import com.library.exception.BookNotFoundException;
import com.library.exception.BorrowerNotFoundException;
import com.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllBooks_shouldReturnAllBooks_whenBooksExist() throws Exception {
        //given
        BookDto book1 = new BookDto(1L, "Book 1", "Author 1", "ISBN1", true, null);
        BookDto book2 = new BookDto(2L, "Book 2", "Author 2", "ISBN2", false, 1L);
        List<BookDto> books = Arrays.asList(book1, book2);
        when(bookService.getAllBooks()).thenReturn(books);

        //when & then
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Book 1"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Book 2"))
                .andExpect(jsonPath("$[1].available").value(false));

        verify(bookService).getAllBooks();
    }

    @Test
    void getAllBooks_shouldReturnEmptyList_whenNoBooksExist() throws Exception {
        //given
        when(bookService.getAllBooks()).thenReturn(Arrays.asList());

        //when & then
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookService).getAllBooks();
    }

    @Test
    void addBook_shouldCreateBook_whenValidBookProvided() throws Exception {
        //given
        BookDto inputBook = new BookDto("New Book", "New Author", "ISBN123");
        BookDto savedBook = new BookDto(1L, "New Book", "New Author", "ISBN123", true, null);
        when(bookService.addBook(any(BookDto.class))).thenReturn(savedBook);

        //when & then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputBook)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Book"))
                .andExpect(jsonPath("$.author").value("New Author"))
                .andExpect(jsonPath("$.isbn").value("ISBN123"))
                .andExpect(jsonPath("$.available").value(true));

        verify(bookService).addBook(any(BookDto.class));
    }

    @Test
    void borrowBook_shouldBorrowBook_whenBookIsAvailable() throws Exception {
        //given
        Long bookId = 1L;
        Long borrowerId = 2L;
        BookDto borrowedBook = new BookDto(bookId, "Test Book", "Test Author", "ISBN123", false, borrowerId);
        when(bookService.borrowBook(bookId, borrowerId)).thenReturn(borrowedBook);

        //when & then
        mockMvc.perform(post("/api/books/{bookId}/borrow/{borrowerId}", bookId, borrowerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.borrowerId").value(borrowerId))
                .andExpect(jsonPath("$.available").value(false));

        verify(bookService).borrowBook(bookId, borrowerId);
    }

    @Test
    void borrowBook_shouldReturnNotFound_whenBookDoesNotExist() throws Exception {
        //given
        Long nonExistentBookId = 999L;
        Long borrowerId = 1L;
        when(bookService.borrowBook(nonExistentBookId, borrowerId))
                .thenThrow(new BookNotFoundException(nonExistentBookId));

        //when & then
        mockMvc.perform(post("/api/books/{bookId}/borrow/{borrowerId}", nonExistentBookId, borrowerId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Book Not Found"))
                .andExpect(jsonPath("$.message").value("Book not found with ID: " + nonExistentBookId));

        verify(bookService).borrowBook(nonExistentBookId, borrowerId);
    }

    @Test
    void borrowBook_shouldReturnNotFound_whenBorrowerDoesNotExist() throws Exception {
        //given
        Long bookId = 1L;
        Long nonExistentBorrowerId = 999L;
        when(bookService.borrowBook(bookId, nonExistentBorrowerId))
                .thenThrow(new BorrowerNotFoundException(nonExistentBorrowerId));

        //when & then
        mockMvc.perform(post("/api/books/{bookId}/borrow/{borrowerId}", bookId, nonExistentBorrowerId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Borrower Not Found"))
                .andExpect(jsonPath("$.message").value("Borrower not found with ID: " + nonExistentBorrowerId));

        verify(bookService).borrowBook(bookId, nonExistentBorrowerId);
    }

    @Test
    void borrowBook_shouldReturnConflict_whenBookAlreadyBorrowed() throws Exception {
        //given
        Long bookId = 1L;
        Long borrowerId = 2L;
        when(bookService.borrowBook(bookId, borrowerId))
                .thenThrow(new BookAlreadyBorrowedException(bookId));

        //when & then
        mockMvc.perform(post("/api/books/{bookId}/borrow/{borrowerId}", bookId, borrowerId))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Book Already Borrowed"))
                .andExpect(jsonPath("$.message").value("Book with ID " + bookId + " is already borrowed"));

        verify(bookService).borrowBook(bookId, borrowerId);
    }
}