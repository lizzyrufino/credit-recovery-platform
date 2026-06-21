package br.com.creditrecovery.processor.rules;

import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.CommunicationChannel;
import br.com.creditrecovery.domain.model.CustomerCreditProfile;
import br.com.creditrecovery.domain.model.RecoveryAction;
import br.com.creditrecovery.domain.rules.RecoveryRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(20)
@Component
public class MediumOverdueCommunicationRule implements RecoveryRule {

    @Override
    public boolean appliesTo(CustomerCreditProfile profile) {
        return profile.daysOverdue() >= 16 && profile.daysOverdue() <= 30;
    }

    @Override
    public RecoveryAction evaluate(CustomerCreditProfile profile) {
        CommunicationChannel channel = profile.whatsappConsent()
                ? CommunicationChannel.WHATSAPP
                : CommunicationChannel.SMS;

        return new RecoveryAction(
                ActionType.COMMUNICATION,
                channel,
                ActionPriority.MEDIUM,
                "Canal com maior chance de engajamento para atraso entre 16 e 30 dias"
        );
    }
}
