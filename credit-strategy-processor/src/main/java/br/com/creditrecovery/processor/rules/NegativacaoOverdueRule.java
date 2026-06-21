package br.com.creditrecovery.processor.rules;

import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.CommunicationChannel;
import br.com.creditrecovery.domain.model.CustomerCreditProfile;
import br.com.creditrecovery.domain.model.RecoveryAction;
import br.com.creditrecovery.domain.rules.RecoveryRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(30)
@Component
public class NegativacaoOverdueRule implements RecoveryRule {

    @Override
    public boolean appliesTo(CustomerCreditProfile profile) {
        return profile.daysOverdue() > 60;
    }

    @Override
    public RecoveryAction evaluate(CustomerCreditProfile profile) {
        return new RecoveryAction(
                ActionType.NEGATIVACAO,
                CommunicationChannel.NONE,
                ActionPriority.HIGH,
                "Cliente PJ com atraso superior a 60 dias"
        );
    }
}
