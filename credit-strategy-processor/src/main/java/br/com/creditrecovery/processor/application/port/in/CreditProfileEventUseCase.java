package br.com.creditrecovery.processor.application.port.in;

import br.com.creditrecovery.domain.event.CreditProfileReceivedEvent;
import br.com.creditrecovery.processor.application.service.ProcessingResult;

public interface CreditProfileEventUseCase {

    ProcessingResult handle(CreditProfileReceivedEvent event);
}
