
package com.creditcard.backend.controller;

import com.creditcard.backend.dto.CreditCardResponse;
import com.creditcard.backend.service.CreditCardService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CreditCardController {

    private final CreditCardService creditCardService;

    public CreditCardController(
            CreditCardService creditCardService) {
        this.creditCardService = creditCardService;
    }

    @GetMapping
    public ResponseEntity<List<CreditCardResponse>> getCards(
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.ok(
                creditCardService.getCardsForUser(userId)
        );
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CreditCardResponse> getCard(
            @PathVariable Long cardId,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.ok(
                creditCardService.getCardForUser(cardId, userId)
        );
    }
}
