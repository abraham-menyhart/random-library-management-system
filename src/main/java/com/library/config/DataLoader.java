package com.library.config;

import com.library.entity.Book;
import com.library.entity.Borrower;
import com.library.repository.BookRepository;
import com.library.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {
    
    private final BorrowerRepository borrowerRepository;
    private final BookRepository bookRepository;
    
    @Override
    public void run(String... args) {
        if (borrowerRepository.count() == 0) {
            loadSampleData();
        }
    }
    
    private void loadSampleData() {
        log.info("Loading sample data...");
        
        // Create borrowers
        Borrower borrower1 = borrowerRepository.save(new Borrower("John Doe", "john.doe@email.com"));
        Borrower borrower2 = borrowerRepository.save(new Borrower("Jane Smith", "jane.smith@email.com"));

        // Create books
        Book book1 = new Book("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565");
        bookRepository.save(book1);
        
        Book book2 = new Book("To Kill a Mockingbird", "Harper Lee", "9780061120084");
        bookRepository.save(book2);
        
        Book book3 = new Book("1984", "George Orwell", "9780451524935");
        book3.setBorrowerId(borrower1.getId());
        bookRepository.save(book3);
        
        Book book4 = new Book("Pride and Prejudice", "Jane Austen", "9780141439518");
        bookRepository.save(book4);
        
        Book book5 = new Book("The Catcher in the Rye", "J.D. Salinger", "9780316769174");
        book5.setBorrowerId(borrower2.getId());
        bookRepository.save(book5);
        
        log.info("Sample data loaded successfully!");
        log.info("Created {} borrowers and {} books", borrowerRepository.count(), bookRepository.count());
    }
}