
package com.creditcard.backend.service;

import com.creditcard.backend.dto.CreditCardResponse;
import com.creditcard.backend.entity.CreditCard;
import com.creditcard.backend.repository.CreditCardRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;

    public CreditCardService(
            CreditCardRepository creditCardRepository) {
        this.creditCardRepository = creditCardRepository;
    }

    public List<CreditCardResponse> getCardsForUser(Long userId) {
        return creditCardRepository.findByUser_Id(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CreditCardResponse getCardForUser(
            Long cardId,
            Long userId) {

        CreditCard card = creditCardRepository
                .findByIdAndUser_Id(cardId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Credit card not found"
                ));

        return toResponse(card);
    }

    private CreditCardResponse toResponse(CreditCard card) {
        return new CreditCardResponse(
                card.getId(),
                card.getBankName(),
                card.getCardName(),
                card.getCardLastFour(),
                card.getCreditLimit()
        );
    }
}
