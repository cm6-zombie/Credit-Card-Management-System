
package com.creditcard.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "credit_cards",
    indexes = {
        @Index(
            name = "idx_credit_cards_user_id",
            columnList = "user_id"
        )
    }
)
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_credit_cards_user"
        )
    )
    private User user;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "card_name", nullable = false, length = 100)
    private String cardName;

    @Column(name = "card_last_four", nullable = false, length = 4)
    private String cardLastFour;

    @Column(
        name = "credit_limit",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal creditLimit;

    protected CreditCard() {
    }

    public CreditCard(
            User user,
            String bankName,
            String cardName,
            String cardLastFour,
            BigDecimal creditLimit) {

        this.user = user;
        this.bankName = bankName;
        this.cardName = cardName;
        this.cardLastFour = cardLastFour;
        this.creditLimit = creditLimit;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getBankName() {
        return bankName;
    }

    public String getCardName() {
        return cardName;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }
}
