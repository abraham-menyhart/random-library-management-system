package com.library.mapper;

import com.library.dto.BorrowerDto;
import com.library.entity.Borrower;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BorrowerMapperTest {

    @InjectMocks
    private BorrowerMapper borrowerMapper;

    @Test
    void toDto_shouldMapEntityToDto_whenBorrowerProvided() {
        //given
        Borrower borrower = new Borrower("John Doe", "john@example.com");
        borrower.setId(1L);

        //when
        BorrowerDto result = borrowerMapper.toDto(borrower);

        //then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void toEntity_shouldMapDtoToEntity_whenBorrowerDtoProvided() {
        //given
        BorrowerDto borrowerDto = new BorrowerDto("Jane Smith", "jane@example.com");

        //when
        Borrower result = borrowerMapper.toEntity(borrowerDto);

        //then
        assertThat(result.getName()).isEqualTo("Jane Smith");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        assertThat(result.getId()).isNull();
    }
}