package br.com.creditrecovery.api.adapter.in.web;

import br.com.creditrecovery.api.adapter.in.web.dto.RecoveryStrategyResponse;
import br.com.creditrecovery.api.adapter.in.web.mapper.RecoveryStrategyDtoMapper;
import br.com.creditrecovery.api.application.port.in.RecoveryStrategyQueryUseCase;
import br.com.creditrecovery.api.validation.ValidCnpj;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Recovery Strategies")
@RequiredArgsConstructor
public class RecoveryStrategyController {

    private final RecoveryStrategyQueryUseCase queryUseCase;
    private final RecoveryStrategyDtoMapper mapper;

    @Timed(value = "strategy.api.latency", extraTags = {"operation", "current"})
    @Operation(summary = "Consulta a estrategia atual de recuperacao de credito do cliente PJ")
    @GetMapping("/{cnpj}/recovery-strategy")
    public RecoveryStrategyResponse currentStrategy(@PathVariable @ValidCnpj String cnpj) {
        return mapper.toResponse(queryUseCase.findCurrent(cnpj));
    }

    @Timed(value = "strategy.api.latency", extraTags = {"operation", "history"})
    @Operation(summary = "Consulta o historico de estrategias de recuperacao de credito do cliente PJ")
    @GetMapping("/{cnpj}/recovery-strategies/history")
    public List<RecoveryStrategyResponse> strategyHistory(@PathVariable @ValidCnpj String cnpj) {
        return queryUseCase.findHistory(cnpj).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
