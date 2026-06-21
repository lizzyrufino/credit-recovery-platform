package br.com.creditrecovery.processor.application.port.out;

import br.com.creditrecovery.domain.event.StrategyGeneratedEvent;

public interface StrategyGeneratedEventPort {

    void publish(StrategyGeneratedEvent event);
}
