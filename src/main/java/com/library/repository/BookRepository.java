package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    List<Book> findByBorrowerId(Long borrowerId);
    
    Optional<Book> findByIsbn(String isbn);
    
    List<Book> findByAvailable(Boolean available);
    
    long countByAvailable(Boolean available);
}