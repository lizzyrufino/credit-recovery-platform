package br.com.creditrecovery.processor.rules;

import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.CommunicationChannel;
import br.com.creditrecovery.domain.enums.RiskLevel;
import br.com.creditrecovery.domain.model.RecoveryAction;
import org.junit.jupiter.api.Test;

import static br.com.creditrecovery.processor.TestProfiles.delayedHistory;
import static br.com.creditrecovery.processor.TestProfiles.profile;
import static br.com.creditrecovery.processor.TestProfiles.regularHistory;
import static org.assertj.core.api.Assertions.assertThat;

class BusinessRulesTest {

    @Test
    void earlyOverdueShouldCreateHomeDigitalEmailAction() {
        var rule = new EarlyOverdueHomeDigitalRule();

        RecoveryAction action = rule.evaluate(profile(10, RiskLevel.LOW, false, false, delayedHistory()));

        assertThat(rule.appliesTo(profile(10, RiskLevel.LOW, false, false, delayedHistory()))).isTrue();
        assertThat(action.type()).isEqualTo(ActionType.HOME_DIGITAL);
        assertThat(action.channel()).isEqualTo(CommunicationChannel.EMAIL);
        assertThat(action.priority()).isEqualTo(ActionPriority.LOW);
    }

    @Test
    void mediumOverdueShouldUseWhatsappWhenConsentExists() {
        var rule = new MediumOverdueCommunicationRule();

        RecoveryAction action = rule.evaluate(profile(20, RiskLevel.MEDIUM, true, false, delayedHistory()));

        assertThat(rule.appliesTo(profile(20, RiskLevel.MEDIUM, true, false, delayedHistory()))).isTrue();
        assertThat(action.type()).isEqualTo(ActionType.COMMUNICATION);
        assertThat(action.channel()).isEqualTo(CommunicationChannel.WHATSAPP);
        assertThat(action.priority()).isEqualTo(ActionPriority.MEDIUM);
    }

    @Test
    void mediumOverdueShouldFallbackToSmsWithoutWhatsappConsent() {
        var rule = new MediumOverdueCommunicationRule();

        RecoveryAction action = rule.evaluate(profile(20, RiskLevel.MEDIUM, false, false, delayedHistory()));

        assertThat(action.channel()).isEqualTo(CommunicationChannel.SMS);
    }

    @Test
    void overdueAboveSixtyDaysShouldCreateNegativacaoAction() {
        var rule = new NegativacaoOverdueRule();

        RecoveryAction action = rule.evaluate(profile(61, RiskLevel.HIGH, true, false, delayedHistory()));

        assertThat(rule.appliesTo(profile(61, RiskLevel.HIGH, true, false, delayedHistory()))).isTrue();
        assertThat(action.type()).isEqualTo(ActionType.NEGATIVACAO);
        assertThat(action.priority()).isEqualTo(ActionPriority.HIGH);
    }

    @Test
    void overdueAboveNinetyDaysShouldDistributeToPartnerOffice() {
        var rule = new PartnerOfficeDistributionRule();

        RecoveryAction action = rule.evaluate(profile(91, RiskLevel.HIGH, true, false, delayedHistory()));

        assertThat(rule.appliesTo(profile(91, RiskLevel.HIGH, true, false, delayedHistory()))).isTrue();
        assertThat(action.type()).isEqualTo(ActionType.DISTRIBUTE_TO_PARTNER_OFFICE);
        assertThat(action.priority()).isEqualTo(ActionPriority.HIGH);
    }

    @Test
    void highRiskCustomerWithActivePjCardShouldBlockCard() {
        var rule = new TemporaryCardBlockRule();

        RecoveryAction action = rule.evaluate(profile(45, RiskLevel.HIGH, true, true, delayedHistory()));

        assertThat(rule.appliesTo(profile(45, RiskLevel.HIGH, true, true, delayedHistory()))).isTrue();
        assertThat(action.type()).isEqualTo(ActionType.TEMPORARY_CARD_BLOCK);
        assertThat(action.priority()).isEqualTo(ActionPriority.HIGH);
    }

    @Test
    void regularizedDebtWithGoodHistoryShouldCreatePositivacaoAction() {
        var rule = new PositivacaoRegularizedDebtRule();

        RecoveryAction action = rule.evaluate(profile(0, RiskLevel.LOW, false, false, regularHistory(true)));

        assertThat(rule.appliesTo(profile(0, RiskLevel.LOW, false, false, regularHistory(true)))).isTrue();
        assertThat(action.type()).isEqualTo(ActionType.POSITIVACAO);
        assertThat(action.priority()).isEqualTo(ActionPriority.MEDIUM);
    }
}
