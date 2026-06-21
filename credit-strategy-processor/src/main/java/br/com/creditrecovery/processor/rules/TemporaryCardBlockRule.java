package br.com.creditrecovery.processor.rules;

import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.CommunicationChannel;
import br.com.creditrecovery.domain.enums.ProductType;
import br.com.creditrecovery.domain.enums.RiskLevel;
import br.com.creditrecovery.domain.model.CustomerCreditProfile;
import br.com.creditrecovery.domain.model.RecoveryAction;
import br.com.creditrecovery.domain.rules.RecoveryRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(50)
@Component
public class TemporaryCardBlockRule implements RecoveryRule {

    @Override
    public boolean appliesTo(CustomerCreditProfile profile) {
        return profile.riskLevel() == RiskLevel.HIGH
                && (profile.activePjCard() || profile.hasActiveProduct(ProductType.CREDIT_CARD_PJ));
    }

    @Override
    public RecoveryAction evaluate(CustomerCreditProfile profile) {
        return new RecoveryAction(
                ActionType.TEMPORARY_CARD_BLOCK,
                CommunicationChannel.NONE,
                ActionPriority.HIGH,
                "Cliente PJ com cartão ativo e risco alto"
        );
    }
}
