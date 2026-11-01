
package com.creditcard.backend.dto;

import java.math.BigDecimal;

public record CreditCardResponse(
        Long id,
        String bankName,
        String cardName,
        String cardLastFour,
        BigDecimal creditLimit
) {
}
