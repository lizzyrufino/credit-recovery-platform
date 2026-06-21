package br.com.creditrecovery.domain.model;

import br.com.creditrecovery.domain.enums.ProductType;

import java.math.BigDecimal;
import java.util.Objects;

public record CreditProduct(
        ProductType type,
        boolean active,
        BigDecimal outstandingAmount
) {

    public CreditProduct {
        Objects.requireNonNull(type, "Product type is required");
        outstandingAmount = outstandingAmount == null ? BigDecimal.ZERO : outstandingAmount;
    }
}
