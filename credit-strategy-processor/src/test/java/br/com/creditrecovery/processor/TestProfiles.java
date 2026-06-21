package br.com.creditrecovery.processor;

import br.com.creditrecovery.domain.enums.CommunicationChannel;
import br.com.creditrecovery.domain.enums.ProductType;
import br.com.creditrecovery.domain.enums.RiskLevel;
import br.com.creditrecovery.domain.model.CreditProduct;
import br.com.creditrecovery.domain.model.CustomerCreditProfile;
import br.com.creditrecovery.domain.model.CustomerDocument;
import br.com.creditrecovery.domain.model.PaymentHistory;

import java.math.BigDecimal;
import java.util.List;

public final class TestProfiles {

    public static final String VALID_CNPJ = "11222333000181";

    private TestProfiles() {
    }

    public static CustomerCreditProfile profile(
            int daysOverdue,
            RiskLevel riskLevel,
            boolean whatsappConsent,
            boolean activePjCard,
            PaymentHistory paymentHistory
    ) {
        return new CustomerCreditProfile(
                CustomerDocument.of(VALID_CNPJ),
                daysOverdue,
                new BigDecimal("120000.00"),
                products(activePjCard),
                812,
                paymentHistory,
                CommunicationChannel.WHATSAPP,
                whatsappConsent,
                riskLevel,
                activePjCard
        );
    }

    private static List<CreditProduct> products(boolean activePjCard) {
        return List.of(
                new CreditProduct(ProductType.CREDIT_CARD_PJ, activePjCard, new BigDecimal("80000.00")),
                new CreditProduct(ProductType.WORKING_CAPITAL, true, new BigDecimal("40000.00"))
        );
    }

    public static PaymentHistory regularHistory(boolean regularized) {
        return new PaymentHistory(12, 0, regularized);
    }

    public static PaymentHistory delayedHistory() {
        return new PaymentHistory(6, 4, false);
    }
}
