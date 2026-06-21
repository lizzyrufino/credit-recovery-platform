package br.com.creditrecovery.processor.engine;

import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.ProductType;
import br.com.creditrecovery.domain.enums.RiskLevel;
import br.com.creditrecovery.domain.rules.RecoveryRule;
import br.com.creditrecovery.processor.application.service.RecoveryStrategyEngine;
import br.com.creditrecovery.processor.rules.EarlyOverdueHomeDigitalRule;
import br.com.creditrecovery.processor.rules.MediumOverdueCommunicationRule;
import br.com.creditrecovery.processor.rules.NegativacaoOverdueRule;
import br.com.creditrecovery.processor.rules.PartnerOfficeDistributionRule;
import br.com.creditrecovery.processor.rules.PositivacaoRegularizedDebtRule;
import br.com.creditrecovery.processor.rules.TemporaryCardBlockRule;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static br.com.creditrecovery.processor.TestProfiles.delayedHistory;
import static br.com.creditrecovery.processor.TestProfiles.profile;
import static br.com.creditrecovery.processor.TestProfiles.regularHistory;
import static org.assertj.core.api.Assertions.assertThat;

class RecoveryStrategyEngineTest {

    private static final Instant FIXED_TIME = Instant.parse("2026-06-20T10:00:00Z");

    private final RecoveryStrategyEngine engine = new RecoveryStrategyEngine(
            rules(),
            new SimpleMeterRegistry(),
            Clock.fixed(FIXED_TIME, ZoneOffset.UTC)
    );

    @Test
    void shouldGenerateStrategyWithAllHighRiskActions() {
        var strategy = engine.generate(profile(95, RiskLevel.HIGH, true, true, delayedHistory()));

        assertThat(strategy.actions()).extracting("type")
                .containsExactly(
                        ActionType.NEGATIVACAO,
                        ActionType.DISTRIBUTE_TO_PARTNER_OFFICE,
                        ActionType.TEMPORARY_CARD_BLOCK
                );
        assertThat(strategy.partnerOffice()).isEqualTo("OFFICE_SPECIALIZED_HIGH_VALUE");
    }

    @Test
    void shouldGenerateEmptyActionListWhenNoRuleApplies() {
        var strategy = engine.generate(profile(0, RiskLevel.LOW, false, false, regularHistory(false)));

        assertThat(strategy.actions()).isEmpty();
        assertThat(strategy.partnerOffice()).isEmpty();
    }

    @Test
    void shouldPopulateStrategyMetadataAndPrimaryProduct() {
        var strategy = engine.generate(profile(12, RiskLevel.LOW, false, true, delayedHistory()));

        assertThat(strategy.generatedAt()).isEqualTo(FIXED_TIME);
        assertThat(strategy.strategyVersion()).isEqualTo("v1");
        assertThat(strategy.productType()).isEqualTo(ProductType.CREDIT_CARD_PJ);
        assertThat(strategy.score()).isEqualTo(812);
    }

    private List<RecoveryRule> rules() {
        return List.of(
                new EarlyOverdueHomeDigitalRule(),
                new MediumOverdueCommunicationRule(),
                new NegativacaoOverdueRule(),
                new PartnerOfficeDistributionRule(),
                new TemporaryCardBlockRule(),
                new PositivacaoRegularizedDebtRule()
        );
    }
}
