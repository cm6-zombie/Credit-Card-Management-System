
package com.creditcard.backend.service;

import com.creditcard.backend.dto.CreditCardResponse;
import com.creditcard.backend.entity.CreditCard;
import com.creditcard.backend.entity.User;
import com.creditcard.backend.repository.CreditCardRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditCardServiceTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private CreditCardService creditCardService;

    private CreditCard card;

    @BeforeEach
    void setUp() {
        User owner = new User(
                "User A",
                "usera@example.com",
                "hashed-password"
        );

        card = new CreditCard(
                owner,
                "HDFC Bank",
                "Millennia",
                "1234",
                new BigDecimal("100000.00")
        );
    }

    @Test
    void shouldReturnCardsBelongingToAuthenticatedUser() {
        when(creditCardRepository.findByUser_Id(1L))
                .thenReturn(List.of(card));

        List<CreditCardResponse> result =
                creditCardService.getCardsForUser(1L);

        assertEquals(1, result.size());
        assertEquals("HDFC Bank", result.get(0).bankName());

        verify(creditCardRepository).findByUser_Id(1L);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoCards() {
        when(creditCardRepository.findByUser_Id(1L))
                .thenReturn(List.of());

        assertTrue(
                creditCardService.getCardsForUser(1L).isEmpty()
        );
    }

    @Test
    void shouldReturnCardWhenOwnershipMatches() {
        when(creditCardRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(card));

        CreditCardResponse result =
                creditCardService.getCardForUser(10L, 1L);

        assertEquals("1234", result.cardLastFour());

        verify(creditCardRepository)
                .findByIdAndUser_Id(10L, 1L);
    }

    @Test
    void shouldRejectCrossUserCardAccess() {
        when(creditCardRepository.findByIdAndUser_Id(10L, 2L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> creditCardService.getCardForUser(10L, 2L)
                );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(creditCardRepository)
                .findByIdAndUser_Id(10L, 2L);

        verify(creditCardRepository, never())
                .findById(anyLong());
    }

    @Test
    void shouldRejectNonexistentCard() {
        when(creditCardRepository.findByIdAndUser_Id(99L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> creditCardService.getCardForUser(99L, 1L)
        );
    }
}
