package br.com.creditrecovery.domain.rules;

import br.com.creditrecovery.domain.model.CustomerCreditProfile;
import br.com.creditrecovery.domain.model.RecoveryAction;

public interface RecoveryRule {

    boolean appliesTo(CustomerCreditProfile profile);

    RecoveryAction evaluate(CustomerCreditProfile profile);
}
