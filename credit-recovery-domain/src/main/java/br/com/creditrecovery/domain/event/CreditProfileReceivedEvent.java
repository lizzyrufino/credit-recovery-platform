package br.com.creditrecovery.domain.event;

import br.com.creditrecovery.domain.model.CustomerCreditProfile;

import java.time.Instant;
import java.util.Objects;

public record CreditProfileReceivedEvent(
        String eventId,
        String correlationId,
        Instant occurredAt,
        CustomerCreditProfile profile
) implements CreditRecoveryDomainEvent {

    public CreditProfileReceivedEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event id is required");
        }
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("Correlation id is required");
        }
        occurredAt = Objects.requireNonNullElseGet(occurredAt, Instant::now);
        Objects.requireNonNull(profile, "Credit profile is required");
    }
}
