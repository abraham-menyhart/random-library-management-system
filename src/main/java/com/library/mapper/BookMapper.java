package com.library.mapper;

import com.library.dto.BookDto;
import com.library.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {
    
    public BookDto toDto(Book book) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getAvailable(),
                book.getBorrowerId()
        );
    }
    
    public Book toEntity(BookDto bookDto) {
        Book book = new Book();
        book.setId(bookDto.getId());
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setIsbn(bookDto.getIsbn());
        book.setAvailable(bookDto.getAvailable() != null ? bookDto.getAvailable() : true);
        book.setBorrowerId(bookDto.getBorrowerId());
        return book;
    }
}