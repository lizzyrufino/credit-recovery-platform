package br.com.creditrecovery.domain.model;

import br.com.creditrecovery.domain.enums.CommunicationChannel;
import br.com.creditrecovery.domain.enums.ProductType;
import br.com.creditrecovery.domain.enums.RiskLevel;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record CustomerCreditProfile(
        CustomerDocument document,
        int daysOverdue,
        BigDecimal debtAmount,
        List<CreditProduct> products,
        int internalScore,
        PaymentHistory paymentHistory,
        CommunicationChannel preferredChannel,
        boolean whatsappConsent,
        RiskLevel riskLevel,
        boolean activePjCard
) {

    public CustomerCreditProfile {
        Objects.requireNonNull(document, "Customer document is required");
        Objects.requireNonNull(paymentHistory, "Payment history is required");
        Objects.requireNonNull(riskLevel, "Risk level is required");
        debtAmount = debtAmount == null ? BigDecimal.ZERO : debtAmount;
        products = products == null ? List.of() : List.copyOf(products);
        preferredChannel = preferredChannel == null ? CommunicationChannel.EMAIL : preferredChannel;
        if (daysOverdue < 0) {
            throw new IllegalArgumentException("Days overdue cannot be negative");
        }
        if (internalScore < 0 || internalScore > 1000) {
            throw new IllegalArgumentException("Internal score must be between 0 and 1000");
        }
    }

    public String cnpj() {
        return document.value();
    }

    public boolean hasActiveProduct(ProductType type) {
        return products.stream().anyMatch(product -> product.type() == type && product.active());
    }

    public ProductType primaryProductType() {
        return products.stream()
                .filter(CreditProduct::active)
                .max(Comparator.comparing(CreditProduct::outstandingAmount))
                .map(CreditProduct::type)
                .orElse(ProductType.OTHER);
    }
}
