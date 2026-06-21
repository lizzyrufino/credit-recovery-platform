package br.com.creditrecovery.domain.model;

import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.CommunicationChannel;

import java.util.Objects;

public record RecoveryAction(
        ActionType type,
        CommunicationChannel channel,
        ActionPriority priority,
        String reason
) {

    public RecoveryAction {
        Objects.requireNonNull(type, "Action type is required");
        Objects.requireNonNull(priority, "Action priority is required");
        channel = channel == null ? CommunicationChannel.NONE : channel;
        reason = reason == null ? "" : reason;
    }
}
