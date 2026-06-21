package br.com.creditrecovery.api.adapter.in.web.dto;

import br.com.creditrecovery.domain.enums.ProductType;
import br.com.creditrecovery.domain.enums.RiskLevel;

import java.time.Instant;
import java.util.List;

public record RecoveryStrategyResponse(
        String cnpj,
        RiskLevel riskLevel,
        int daysOverdue,
        ProductType productType,
        List<RecoveryActionResponse> actions,
        String partnerOffice,
        String strategyVersion,
        Instant generatedAt
) {
}
