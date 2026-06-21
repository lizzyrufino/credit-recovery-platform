package br.com.creditrecovery.domain.event;

import java.time.Instant;

public sealed interface CreditRecoveryDomainEvent permits CreditProfileReceivedEvent, StrategyGeneratedEvent {

    String eventId();

    String correlationId();

    Instant occurredAt();
}
