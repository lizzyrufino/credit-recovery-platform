package br.com.creditrecovery.domain.event;

import br.com.creditrecovery.domain.enums.StrategyStatus;

import java.time.Instant;
import java.util.Objects;

public record StrategyGeneratedEvent(
        String eventId,
        String correlationId,
        Instant occurredAt,
        String cnpj,
        Instant generatedAt,
        StrategyStatus status
) implements CreditRecoveryDomainEvent {

    public StrategyGeneratedEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event id is required");
        }
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("Correlation id is required");
        }
        if (cnpj == null || cnpj.isBlank()) {
            throw new IllegalArgumentException("CNPJ is required");
        }
        occurredAt = Objects.requireNonNullElseGet(occurredAt, Instant::now);
        generatedAt = Objects.requireNonNullElseGet(generatedAt, Instant::now);
        status = Objects.requireNonNullElse(status, StrategyStatus.ACTIVE);
    }
}
