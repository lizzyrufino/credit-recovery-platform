package br.com.creditrecovery.domain.model;

import br.com.creditrecovery.domain.enums.ProductType;
import br.com.creditrecovery.domain.enums.RiskLevel;
import br.com.creditrecovery.domain.enums.StrategyStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record RecoveryStrategy(
        CustomerDocument document,
        RiskLevel riskLevel,
        int daysOverdue,
        BigDecimal debtAmount,
        ProductType productType,
        int score,
        List<RecoveryAction> actions,
        String partnerOffice,
        String strategyVersion,
        StrategyStatus status,
        Instant generatedAt
) {

    public RecoveryStrategy {
        Objects.requireNonNull(document, "Customer document is required");
        Objects.requireNonNull(riskLevel, "Risk level is required");
        Objects.requireNonNull(productType, "Product type is required");
        Objects.requireNonNull(status, "Strategy status is required");
        Objects.requireNonNull(generatedAt, "Generated timestamp is required");
        debtAmount = debtAmount == null ? BigDecimal.ZERO : debtAmount;
        actions = actions == null ? List.of() : List.copyOf(actions);
        partnerOffice = partnerOffice == null ? "" : partnerOffice;
        strategyVersion = strategyVersion == null ? "v1" : strategyVersion;
    }

    public String cnpj() {
        return document.value();
    }
}
