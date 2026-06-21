package br.com.creditrecovery.api.application.port.out;

import br.com.creditrecovery.domain.model.CustomerDocument;
import br.com.creditrecovery.domain.model.RecoveryStrategy;

import java.util.List;
import java.util.Optional;

public interface RecoveryStrategyLookupPort {

    Optional<RecoveryStrategy> findCurrentByCnpj(CustomerDocument document);

    List<RecoveryStrategy> findHistoryByCnpj(CustomerDocument document);
}
