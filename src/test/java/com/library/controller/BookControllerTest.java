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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllBooks_shouldReturnOk_whenBooksExist() throws Exception {
        //given
        BookDto book1 = new BookDto(1L, "Book 1", "Author 1", "ISBN1", true, null);
        BookDto book2 = new BookDto(2L, "Book 2", "Author 2", "ISBN2", false, 1L);
        List<BookDto> books = Arrays.asList(book1, book2);
        when(bookService.getAllBooks()).thenReturn(books);

        //when & then
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());

        verify(bookService).getAllBooks();
    }

    @Test
    void getAllBooks_shouldReturnOk_whenNoBooksExist() throws Exception {
        //given
        when(bookService.getAllBooks()).thenReturn(Arrays.asList());

        //when & then
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());

        verify(bookService).getAllBooks();
    }

    @Test
    void addBook_shouldReturnCreated_whenValidBookProvided() throws Exception {
        //given
        BookDto inputBook = new BookDto("New Book", "New Author", "ISBN123");
        BookDto savedBook = new BookDto(1L, "New Book", "New Author", "ISBN123", true, null);
        when(bookService.addBook(any(BookDto.class))).thenReturn(savedBook);

        //when & then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputBook)))
                .andExpect(status().isCreated());

        verify(bookService).addBook(any(BookDto.class));
    }

    @Test
    void borrowBook_shouldReturnOk_whenBookIsAvailable() throws Exception {
        //given
        Long bookId = 1L;
        Long borrowerId = 2L;
        BookDto borrowedBook = new BookDto(bookId, "Test Book", "Test Author", "ISBN123", false, borrowerId);
        when(bookService.borrowBook(bookId, borrowerId)).thenReturn(borrowedBook);

        //when & then
        mockMvc.perform(post("/api/books/{bookId}/borrow/{borrowerId}", bookId, borrowerId))
                .andExpect(status().isOk());

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
                .andExpect(status().isNotFound());

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
                .andExpect(status().isNotFound());

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
                .andExpect(status().isConflict());

        verify(bookService).borrowBook(bookId, borrowerId);
    }
}