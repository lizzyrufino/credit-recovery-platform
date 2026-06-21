package br.com.creditrecovery.processor.application.port.out;

import br.com.creditrecovery.domain.event.CreditProfileReceivedEvent;
import br.com.creditrecovery.domain.model.RecoveryStrategy;

public interface RecoveryStrategyPersistencePort {

    boolean isEventProcessed(String eventId);

    void saveStrategyAndMarkEvent(RecoveryStrategy strategy, CreditProfileReceivedEvent event);
}
