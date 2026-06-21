package br.com.creditrecovery.api.adapter.in.web.dto;

import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.CommunicationChannel;

public record RecoveryActionResponse(
        ActionType type,
        CommunicationChannel channel,
        ActionPriority priority,
        String reason
) {
}
