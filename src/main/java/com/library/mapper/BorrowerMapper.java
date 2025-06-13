package com.library.mapper;

import com.library.dto.BorrowerDto;
import com.library.entity.Borrower;
import org.springframework.stereotype.Component;

@Component
public class BorrowerMapper {
    
    public BorrowerDto toDto(Borrower borrower) {
        return new BorrowerDto(
                borrower.getId(),
                borrower.getName(),
                borrower.getEmail()
        );
    }
    
    public Borrower toEntity(BorrowerDto borrowerDto) {
        Borrower borrower = new Borrower();
        borrower.setId(borrowerDto.getId());
        borrower.setName(borrowerDto.getName());
        borrower.setEmail(borrowerDto.getEmail());
        return borrower;
    }
}