package com.library.repository;

import com.library.entity.Borrower;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BorrowerRepositoryTest {

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Test
    void findByEmail_shouldReturnBorrower_whenEmailExists() {
        //given
        String email = "test@example.com";
        Borrower borrower = new Borrower("Test User", email);
        borrowerRepository.save(borrower);
        
        //when
        Optional<Borrower> foundBorrower = borrowerRepository.findByEmail(email);
        
        //then
        assertThat(foundBorrower).isPresent();
        assertThat(foundBorrower.get().getEmail()).isEqualTo(email);
        assertThat(foundBorrower.get().getName()).isEqualTo("Test User");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        //given
        String nonExistentEmail = "nonexistent@example.com";
        
        //when
        Optional<Borrower> foundBorrower = borrowerRepository.findByEmail(nonExistentEmail);
        
        //then
        assertThat(foundBorrower).isEmpty();
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        //given
        String email = "existing@example.com";
        Borrower borrower = new Borrower("Existing User", email);
        borrowerRepository.save(borrower);
        
        //when
        boolean exists = borrowerRepository.existsByEmail(email);
        
        //then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        //given
        String nonExistentEmail = "nonexistent@example.com";
        
        //when
        boolean exists = borrowerRepository.existsByEmail(nonExistentEmail);
        
        //then
        assertThat(exists).isFalse();
    }

    @Test
    void save_shouldPersistBorrower_whenValidBorrowerProvided() {
        //given
        Borrower borrower = new Borrower("New User", "newuser@example.com");
        
        //when
        Borrower savedBorrower = borrowerRepository.save(borrower);
        
        //then
        assertThat(savedBorrower.getId()).isNotNull();
        assertThat(savedBorrower.getName()).isEqualTo("New User");
        assertThat(savedBorrower.getEmail()).isEqualTo("newuser@example.com");
        
        Optional<Borrower> foundBorrower = borrowerRepository.findById(savedBorrower.getId());
        assertThat(foundBorrower).isPresent();
        assertThat(foundBorrower.get().getName()).isEqualTo("New User");
    }
}