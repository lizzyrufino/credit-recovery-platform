package br.com.creditrecovery.api.controller;

import br.com.creditrecovery.api.adapter.in.web.RecoveryStrategyController;
import br.com.creditrecovery.api.adapter.in.web.exception.StrategyNotFoundException;
import br.com.creditrecovery.api.adapter.in.web.mapper.RecoveryStrategyDtoMapper;
import br.com.creditrecovery.api.application.port.in.RecoveryStrategyQueryUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static br.com.creditrecovery.api.TestStrategies.VALID_CNPJ;
import static br.com.creditrecovery.api.TestStrategies.strategy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecoveryStrategyController.class)
@AutoConfigureMockMvc
@Import(RecoveryStrategyDtoMapper.class)
@WithMockUser
class RecoveryStrategyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecoveryStrategyQueryUseCase queryService;

    @Test
    void shouldReturnCurrentStrategy() throws Exception {
        when(queryService.findCurrent(VALID_CNPJ)).thenReturn(strategy());

        mockMvc.perform(get("/api/v1/customers/{cnpj}/recovery-strategy", VALID_CNPJ)
                        .header("X-Correlation-Id", "corr-test"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "corr-test"))
                .andExpect(jsonPath("$.cnpj").value(VALID_CNPJ))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.actions[0].type").value("NEGATIVACAO"));
    }

    @Test
    void shouldReturnNotFoundWhenStrategyDoesNotExist() throws Exception {
        when(queryService.findCurrent(VALID_CNPJ)).thenThrow(new StrategyNotFoundException("hash"));

        mockMvc.perform(get("/api/v1/customers/{cnpj}/recovery-strategy", VALID_CNPJ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void shouldReturnBadRequestForInvalidCnpj() throws Exception {
        mockMvc.perform(get("/api/v1/customers/{cnpj}/recovery-strategy", "11111111111111"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(queryService, never()).findCurrent("11111111111111");
    }
}
