package com.library.mapper;

import com.library.dto.BookDto;
import com.library.entity.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BookMapperTest {

    @InjectMocks
    private BookMapper bookMapper;

    @Test
    void toDto_shouldMapEntityToDto_whenBookProvided() {
        //given
        Book book = new Book("Test Book", "Test Author", "1234567890");
        book.setId(1L);
        book.setBorrowerId(2L);

        //when
        BookDto result = bookMapper.toDto(book);

        //then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Book");
        assertThat(result.getAuthor()).isEqualTo("Test Author");
        assertThat(result.getIsbn()).isEqualTo("1234567890");
        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getBorrowerId()).isEqualTo(2L);
    }

    @Test
    void toEntity_shouldMapDtoToEntity_whenBookDtoProvided() {
        //given
        BookDto bookDto = new BookDto("Test Book", "Test Author", "1234567890");

        //when
        Book result = bookMapper.toEntity(bookDto);

        //then
        assertThat(result.getTitle()).isEqualTo("Test Book");
        assertThat(result.getAuthor()).isEqualTo("Test Author");
        assertThat(result.getIsbn()).isEqualTo("1234567890");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getBorrowerId()).isNull();
    }
}