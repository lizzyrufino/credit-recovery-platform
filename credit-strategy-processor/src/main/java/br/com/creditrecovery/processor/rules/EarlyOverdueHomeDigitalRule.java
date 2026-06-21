package br.com.creditrecovery.processor.rules;

import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.CommunicationChannel;
import br.com.creditrecovery.domain.model.CustomerCreditProfile;
import br.com.creditrecovery.domain.model.RecoveryAction;
import br.com.creditrecovery.domain.rules.RecoveryRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(10)
@Component
public class EarlyOverdueHomeDigitalRule implements RecoveryRule {

    @Override
    public boolean appliesTo(CustomerCreditProfile profile) {
        return profile.daysOverdue() >= 1 && profile.daysOverdue() <= 15;
    }

    @Override
    public RecoveryAction evaluate(CustomerCreditProfile profile) {
        return new RecoveryAction(
                ActionType.HOME_DIGITAL,
                CommunicationChannel.EMAIL,
                ActionPriority.LOW,
                "Cliente PJ com atraso inicial entre 1 e 15 dias"
        );
    }
}
