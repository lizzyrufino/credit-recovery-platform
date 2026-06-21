package br.com.creditrecovery.processor.rules;

import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.CommunicationChannel;
import br.com.creditrecovery.domain.model.CustomerCreditProfile;
import br.com.creditrecovery.domain.model.RecoveryAction;
import br.com.creditrecovery.domain.rules.RecoveryRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(60)
@Component
public class PositivacaoRegularizedDebtRule implements RecoveryRule {

    @Override
    public boolean appliesTo(CustomerCreditProfile profile) {
        return profile.paymentHistory().debtRegularized() && profile.paymentHistory().hasGoodHistory();
    }

    @Override
    public RecoveryAction evaluate(CustomerCreditProfile profile) {
        return new RecoveryAction(
                ActionType.POSITIVACAO,
                CommunicationChannel.NONE,
                ActionPriority.MEDIUM,
                "Cliente PJ com bom histórico e dívida regularizada"
        );
    }
}
