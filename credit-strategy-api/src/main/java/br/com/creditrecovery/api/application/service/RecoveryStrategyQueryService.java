package br.com.creditrecovery.api.application.service;

import br.com.creditrecovery.api.adapter.in.web.exception.StrategyNotFoundException;
import br.com.creditrecovery.api.application.port.in.RecoveryStrategyQueryUseCase;
import br.com.creditrecovery.api.application.port.out.RecoveryStrategyLookupPort;
import br.com.creditrecovery.domain.model.CustomerDocument;
import br.com.creditrecovery.domain.model.RecoveryStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryStrategyQueryService implements RecoveryStrategyQueryUseCase {

    private final RecoveryStrategyLookupPort lookupPort;

    @Override
    public RecoveryStrategy findCurrent(String cnpj) {
        CustomerDocument document = CustomerDocument.of(cnpj);
        return lookupPort.findCurrentByCnpj(document)
                .map(strategy -> {
                    log.info("current_strategy_found customerHash={}", document.hashed());
                    return strategy;
                })
                .orElseThrow(() -> new StrategyNotFoundException(document.hashed()));
    }

    @Override
    public List<RecoveryStrategy> findHistory(String cnpj) {
        CustomerDocument document = CustomerDocument.of(cnpj);
        List<RecoveryStrategy> history = lookupPort.findHistoryByCnpj(document);
        log.info("strategy_history_found customerHash={} count={}", document.hashed(), history.size());
        return history;
    }
}
