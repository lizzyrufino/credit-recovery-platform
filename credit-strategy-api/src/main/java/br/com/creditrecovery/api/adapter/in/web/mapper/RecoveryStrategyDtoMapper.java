package br.com.creditrecovery.api.adapter.in.web.mapper;

import br.com.creditrecovery.api.adapter.in.web.dto.RecoveryActionResponse;
import br.com.creditrecovery.api.adapter.in.web.dto.RecoveryStrategyResponse;
import br.com.creditrecovery.domain.model.RecoveryStrategy;
import org.springframework.stereotype.Component;

@Component
public class RecoveryStrategyDtoMapper {

    public RecoveryStrategyResponse toResponse(RecoveryStrategy strategy) {
        return new RecoveryStrategyResponse(
                strategy.cnpj(),
                strategy.riskLevel(),
                strategy.daysOverdue(),
                strategy.productType(),
                strategy.actions().stream()
                        .map(action -> new RecoveryActionResponse(
                                action.type(),
                                action.channel(),
                                action.priority(),
                                action.reason()))
                        .toList(),
                strategy.partnerOffice(),
                strategy.strategyVersion(),
                strategy.generatedAt()
        );
    }
}
