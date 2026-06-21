package br.com.creditrecovery.processor.application.service;

import br.com.creditrecovery.domain.event.CreditProfileReceivedEvent;
import br.com.creditrecovery.domain.event.StrategyGeneratedEvent;
import br.com.creditrecovery.domain.model.RecoveryStrategy;
import br.com.creditrecovery.processor.adapter.out.dynamodb.AlreadyProcessedEventException;
import br.com.creditrecovery.processor.application.port.in.CreditProfileEventUseCase;
import br.com.creditrecovery.processor.application.port.out.RecoveryStrategyPersistencePort;
import br.com.creditrecovery.processor.application.port.out.StrategyGeneratedEventPort;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditProfileEventHandler implements CreditProfileEventUseCase {

    private final RecoveryStrategyPersistencePort persistencePort;
    private final RecoveryStrategyEngine strategyEngine;
    private final StrategyGeneratedEventPort eventPort;
    private final MeterRegistry meterRegistry;
    private final Clock clock;

    @Override
    public ProcessingResult handle(CreditProfileReceivedEvent event) {
        MDC.put("correlationId", event.correlationId());
        String customerHash = event.profile().document().hashed();
        try {
            if (persistencePort.isEventProcessed(event.eventId())) {
                log.info("duplicate_credit_profile_event_ignored eventId={} customerHash={}", event.eventId(), customerHash);
                return ProcessingResult.DUPLICATE;
            }

            RecoveryStrategy strategy = strategyEngine.generate(event.profile());
            persistencePort.saveStrategyAndMarkEvent(strategy, event);
            publishGeneratedEvent(event, strategy);

            meterRegistry.counter("strategy.generated.count").increment();
            log.info("strategy_generated eventId={} customerHash={} actions={}",
                    event.eventId(),
                    customerHash,
                    strategy.actions().size());
            return ProcessingResult.PROCESSED;
        } catch (AlreadyProcessedEventException exception) {
            log.info("duplicate_credit_profile_event_race_ignored eventId={} customerHash={}", event.eventId(), customerHash);
            return ProcessingResult.DUPLICATE;
        } catch (RuntimeException exception) {
            meterRegistry.counter("strategy.generation.failed.count").increment();
            log.error("strategy_generation_failed eventId={} customerHash={}", event.eventId(), customerHash, exception);
            throw exception;
        } finally {
            MDC.remove("correlationId");
        }
    }

    private void publishGeneratedEvent(CreditProfileReceivedEvent event, RecoveryStrategy strategy) {
        eventPort.publish(new StrategyGeneratedEvent(
                UUID.randomUUID().toString(),
                event.correlationId(),
                Instant.now(clock),
                strategy.cnpj(),
                strategy.generatedAt(),
                strategy.status()
        ));
    }
}
