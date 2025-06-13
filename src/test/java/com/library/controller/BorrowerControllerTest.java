package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.BookDto;
import com.library.dto.BorrowerDto;
import com.library.exception.BorrowerNotFoundException;
import com.library.exception.DuplicateEmailException;
import com.library.service.BorrowerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowerController.class)
class BorrowerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowerService borrowerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllBorrowers_shouldReturnAllBorrowers_whenBorrowersExist() throws Exception {
        //given
        BorrowerDto borrower1 = new BorrowerDto(1L, "John Doe", "john@example.com");
        BorrowerDto borrower2 = new BorrowerDto(2L, "Jane Smith", "jane@example.com");
        List<BorrowerDto> borrowers = Arrays.asList(borrower1, borrower2);
        when(borrowerService.getAllBorrowers()).thenReturn(borrowers);

        //when & then
        mockMvc.perform(get("/api/borrowers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john@example.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"))
                .andExpect(jsonPath("$[1].email").value("jane@example.com"));

        verify(borrowerService).getAllBorrowers();
    }

    @Test
    void getAllBorrowers_shouldReturnEmptyList_whenNoBorrowersExist() throws Exception {
        //given
        when(borrowerService.getAllBorrowers()).thenReturn(Arrays.asList());

        //when & then
        mockMvc.perform(get("/api/borrowers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(borrowerService).getAllBorrowers();
    }

    @Test
    void createBorrower_shouldCreateBorrower_whenValidBorrowerProvided() throws Exception {
        //given
        BorrowerDto inputBorrower = new BorrowerDto("John Doe", "john@example.com");
        BorrowerDto savedBorrower = new BorrowerDto(1L, "John Doe", "john@example.com");
        when(borrowerService.createBorrower(any(BorrowerDto.class))).thenReturn(savedBorrower);

        //when & then
        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputBorrower)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(borrowerService).createBorrower(any(BorrowerDto.class));
    }

    @Test
    void createBorrower_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
        //given
        BorrowerDto inputBorrower = new BorrowerDto("John Doe", "existing@example.com");
        when(borrowerService.createBorrower(any(BorrowerDto.class)))
                .thenThrow(new DuplicateEmailException("Email already exists: existing@example.com"));

        //when & then
        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputBorrower)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Duplicate Email"))
                .andExpect(jsonPath("$.message").value("Email already exists: existing@example.com"));

        verify(borrowerService).createBorrower(any(BorrowerDto.class));
    }

    @Test
    void getBorrower_shouldReturnBorrower_whenBorrowerExists() throws Exception {
        //given
        Long borrowerId = 1L;
        BorrowerDto borrower = new BorrowerDto(borrowerId, "John Doe", "john@example.com");
        when(borrowerService.getBorrower(borrowerId)).thenReturn(borrower);

        //when & then
        mockMvc.perform(get("/api/borrowers/{id}", borrowerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(borrowerId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(borrowerService).getBorrower(borrowerId);
    }

    @Test
    void getBorrower_shouldReturnNotFound_whenBorrowerDoesNotExist() throws Exception {
        //given
        Long nonExistentBorrowerId = 999L;
        when(borrowerService.getBorrower(nonExistentBorrowerId))
                .thenThrow(new BorrowerNotFoundException(nonExistentBorrowerId));

        //when & then
        mockMvc.perform(get("/api/borrowers/{id}", nonExistentBorrowerId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Borrower Not Found"))
                .andExpect(jsonPath("$.message").value("Borrower not found with ID: " + nonExistentBorrowerId));

        verify(borrowerService).getBorrower(nonExistentBorrowerId);
    }

    @Test
    void getBorrowedBooks_shouldReturnBorrowedBooks_whenBorrowerExists() throws Exception {
        //given
        Long borrowerId = 1L;
        BookDto book1 = new BookDto(1L, "Book 1", "Author 1", "ISBN1", false, borrowerId);
        BookDto book2 = new BookDto(2L, "Book 2", "Author 2", "ISBN2", false, borrowerId);
        List<BookDto> borrowedBooks = Arrays.asList(book1, book2);
        when(borrowerService.getBorrowedBooks(borrowerId)).thenReturn(borrowedBooks);

        //when & then
        mockMvc.perform(get("/api/borrowers/{id}/books", borrowerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].borrowerId").value(borrowerId))
                .andExpect(jsonPath("$[0].available").value(false))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].borrowerId").value(borrowerId))
                .andExpect(jsonPath("$[1].available").value(false));

        verify(borrowerService).getBorrowedBooks(borrowerId);
    }

    @Test
    void getBorrowedBooks_shouldReturnNotFound_whenBorrowerDoesNotExist() throws Exception {
        //given
        Long nonExistentBorrowerId = 999L;
        when(borrowerService.getBorrowedBooks(nonExistentBorrowerId))
                .thenThrow(new BorrowerNotFoundException(nonExistentBorrowerId));

        //when & then
        mockMvc.perform(get("/api/borrowers/{id}/books", nonExistentBorrowerId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Borrower Not Found"))
                .andExpect(jsonPath("$.message").value("Borrower not found with ID: " + nonExistentBorrowerId));

        verify(borrowerService).getBorrowedBooks(nonExistentBorrowerId);
    }
}