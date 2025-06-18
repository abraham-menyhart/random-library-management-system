package com.library.integration;

import com.library.controller.ErrorResponse;
import com.library.dto.BookDto;
import com.library.dto.BorrowerDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LibraryE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api";
    }

    @Test
    void borrowBook_shouldPreventDoubleBorrowing_whenBookAlreadyBorrowed() {
        //given
        BookDto book = createBook("The Great Gatsby", "F. Scott Fitzgerald");
        BorrowerDto firstBorrower = createBorrower("John Smith", "john.smith");
        BorrowerDto secondBorrower = createBorrower("Jane Doe", "jane.doe");
        borrowBook(book.getId(), firstBorrower.getId());

        //when
        ErrorResponse errorResponse = attemptToBorrowBook(book.getId(), secondBorrower.getId());

        //then
        assertThat(errorResponse.getError()).isEqualTo("Book Already Borrowed");
        assertThat(errorResponse.getMessage()).contains("Book with ID " + book.getId() + " is already borrowed");
    }

    @Test
    void getAllBooks_shouldReturnBorrowedAndAvailableBooks() {
        //given
        String uniqueTitle1 = "Test Book 1984 " + System.currentTimeMillis();
        String uniqueTitle2 = "Test Book Mockingbird " + System.currentTimeMillis();
        BookDto book1 = createBook(uniqueTitle1, "George Orwell");
        BookDto book2 = createBook(uniqueTitle2, "Harper Lee");
        BorrowerDto borrower = createBorrower("Alice Johnson", "alice");
        borrowBook(book1.getId(), borrower.getId());

        //when
        BookDto[] allBooks = getAllBooks();

        //then - should return both books with correct availability status
        assertThat(allBooks).hasSizeGreaterThanOrEqualTo(2);
        BookDto borrowedBook = findBookByTitle(allBooks, uniqueTitle1);
        BookDto availableBook = findBookByTitle(allBooks, uniqueTitle2);
        assertThat(borrowedBook.getAvailable()).isFalse();
        assertThat(borrowedBook.getBorrowerId()).isEqualTo(borrower.getId());
        assertThat(availableBook.getAvailable()).isTrue();
        assertThat(availableBook.getBorrowerId()).isNull();
    }

    @Test
    void getBorrowedBooks_shouldReturnAllBooksBorrowedByUser() {
        //given
        BorrowerDto borrower = createBorrower("Bob Wilson", "bob");
        BookDto book1 = createBook("Clean Code", "Robert Martin");
        BookDto book2 = createBook("Design Patterns", "Gang of Four");
        BookDto book3 = createBook("Refactoring", "Martin Fowler");
        borrowBook(book1.getId(), borrower.getId());
        borrowBook(book2.getId(), borrower.getId());

        //when
        BookDto[] borrowedBooks = getBorrowedBooks(borrower.getId());

        //then - should return exactly the two borrowed books
        assertThat(borrowedBooks).hasSize(2);
        assertThat(borrowedBooks)
                .extracting(BookDto::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Design Patterns");
        assertThat(borrowedBooks)
                .allMatch(book -> book.getBorrowerId().equals(borrower.getId()))
                .allMatch(book -> !book.getAvailable());
    }

    private BookDto findBookByTitle(BookDto[] books, String title) {
        for (BookDto book : books) {
            if (book.getTitle().equals(title)) {
                return book;
            }
        }
        throw new AssertionError("Book with title '" + title + "' not found");
    }

    // Helper methods for creating test data
    private BookDto createBook(String title, String author) {
        String uniqueIsbn = "978-TEST-" + System.currentTimeMillis();
        BookDto book = new BookDto(title, author, uniqueIsbn);
        ResponseEntity<BookDto> response = restTemplate.postForEntity(
                baseUrl() + "/books",
                book,
                BookDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private BorrowerDto createBorrower(String name, String emailPrefix) {
        String uniqueEmail = emailPrefix + System.currentTimeMillis() + "@email.com";
        BorrowerDto borrower = new BorrowerDto(name, uniqueEmail);
        ResponseEntity<BorrowerDto> response = restTemplate.postForEntity(
                baseUrl() + "/borrowers",
                borrower,
                BorrowerDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private void borrowBook(Long bookId, Long borrowerId) {
        ResponseEntity<BookDto> response = restTemplate.postForEntity(
                baseUrl() + "/books/" + bookId + "/borrow/" + borrowerId,
                null,
                BookDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private ErrorResponse attemptToBorrowBook(Long bookId, Long borrowerId) {
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                baseUrl() + "/books/" + bookId + "/borrow/" + borrowerId,
                null,
                ErrorResponse.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        return response.getBody();
    }

    private BookDto[] getAllBooks() {
        ResponseEntity<BookDto[]> response = restTemplate.getForEntity(
                baseUrl() + "/books",
                BookDto[].class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private BookDto[] getBorrowedBooks(Long borrowerId) {
        ResponseEntity<BookDto[]> response = restTemplate.getForEntity(
                baseUrl() + "/borrowers/" + borrowerId + "/books",
                BookDto[].class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }
}