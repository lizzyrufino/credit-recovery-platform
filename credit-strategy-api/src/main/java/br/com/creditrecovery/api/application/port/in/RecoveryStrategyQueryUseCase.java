package br.com.creditrecovery.api.application.port.in;

import br.com.creditrecovery.domain.model.RecoveryStrategy;

import java.util.List;

public interface RecoveryStrategyQueryUseCase {

    RecoveryStrategy findCurrent(String cnpj);

    List<RecoveryStrategy> findHistory(String cnpj);
}
