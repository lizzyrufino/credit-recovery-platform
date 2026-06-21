package br.com.creditrecovery.processor.adapter.out.events;

import br.com.creditrecovery.domain.event.StrategyGeneratedEvent;
import br.com.creditrecovery.processor.application.port.out.StrategyGeneratedEventPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingStrategyGeneratedEventAdapter implements StrategyGeneratedEventPort {

    @Override
    public void publish(StrategyGeneratedEvent event) {
        log.info("strategy_generated_event_ready eventId={} correlationId={} status={}",
                event.eventId(),
                event.correlationId(),
                event.status());
    }
}
