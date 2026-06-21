package br.com.creditrecovery.api;

import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.CommunicationChannel;
import br.com.creditrecovery.domain.enums.ProductType;
import br.com.creditrecovery.domain.enums.RiskLevel;
import br.com.creditrecovery.domain.enums.StrategyStatus;
import br.com.creditrecovery.domain.model.CustomerDocument;
import br.com.creditrecovery.domain.model.RecoveryAction;
import br.com.creditrecovery.domain.model.RecoveryStrategy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class TestStrategies {

    public static final String VALID_CNPJ = "11222333000181";
    public static final Instant GENERATED_AT = Instant.parse("2026-06-20T10:00:00Z");

    private TestStrategies() {
    }

    public static RecoveryStrategy strategy() {
        return new RecoveryStrategy(
                CustomerDocument.of(VALID_CNPJ),
                RiskLevel.HIGH,
                87,
                new BigDecimal("125000.50"),
                ProductType.CREDIT_CARD_PJ,
                812,
                List.of(
                        new RecoveryAction(
                                ActionType.NEGATIVACAO,
                                CommunicationChannel.NONE,
                                ActionPriority.HIGH,
                                "Cliente PJ com atraso superior a 60 dias"
                        ),
                        new RecoveryAction(
                                ActionType.COMMUNICATION,
                                CommunicationChannel.WHATSAPP,
                                ActionPriority.MEDIUM,
                                "Canal com maior chance de engajamento"
                        )
                ),
                "OFFICE_A",
                "v1",
                StrategyStatus.ACTIVE,
                GENERATED_AT
        );
    }
}
