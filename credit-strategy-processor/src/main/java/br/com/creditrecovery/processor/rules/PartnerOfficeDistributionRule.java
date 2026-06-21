package br.com.creditrecovery.processor.rules;

import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.CommunicationChannel;
import br.com.creditrecovery.domain.model.CustomerCreditProfile;
import br.com.creditrecovery.domain.model.RecoveryAction;
import br.com.creditrecovery.domain.rules.RecoveryRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(40)
@Component
public class PartnerOfficeDistributionRule implements RecoveryRule {

    @Override
    public boolean appliesTo(CustomerCreditProfile profile) {
        return profile.daysOverdue() > 90;
    }

    @Override
    public RecoveryAction evaluate(CustomerCreditProfile profile) {
        return new RecoveryAction(
                ActionType.DISTRIBUTE_TO_PARTNER_OFFICE,
                CommunicationChannel.NONE,
                ActionPriority.HIGH,
                "Cliente PJ com atraso superior a 90 dias elegível para escritório parceiro"
        );
    }
}
