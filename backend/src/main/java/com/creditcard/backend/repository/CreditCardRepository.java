
package com.creditcard.backend.repository;

import com.creditcard.backend.entity.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditCardRepository
        extends JpaRepository<CreditCard, Long> {

    List<CreditCard> findByUser_Id(Long userId);

    Optional<CreditCard> findByIdAndUser_Id(
            Long cardId,
            Long userId
    );

    boolean existsByIdAndUser_Id(
            Long cardId,
            Long userId
    );
}
