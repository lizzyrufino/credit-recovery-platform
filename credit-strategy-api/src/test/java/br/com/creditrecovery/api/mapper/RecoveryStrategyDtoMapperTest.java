package br.com.creditrecovery.api.mapper;

import br.com.creditrecovery.api.adapter.in.web.mapper.RecoveryStrategyDtoMapper;
import br.com.creditrecovery.domain.enums.ActionType;
import org.junit.jupiter.api.Test;

import static br.com.creditrecovery.api.TestStrategies.GENERATED_AT;
import static br.com.creditrecovery.api.TestStrategies.VALID_CNPJ;
import static br.com.creditrecovery.api.TestStrategies.strategy;
import static org.assertj.core.api.Assertions.assertThat;

class RecoveryStrategyDtoMapperTest {

    @Test
    void shouldMapDomainStrategyToResponseDto() {
        var response = new RecoveryStrategyDtoMapper().toResponse(strategy());

        assertThat(response.cnpj()).isEqualTo(VALID_CNPJ);
        assertThat(response.generatedAt()).isEqualTo(GENERATED_AT);
        assertThat(response.actions()).hasSize(2);
        assertThat(response.actions().getFirst().type()).isEqualTo(ActionType.NEGATIVACAO);
    }
}
