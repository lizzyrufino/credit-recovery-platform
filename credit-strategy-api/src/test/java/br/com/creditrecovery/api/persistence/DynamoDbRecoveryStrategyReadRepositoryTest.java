package br.com.creditrecovery.api.persistence;

import br.com.creditrecovery.api.adapter.out.dynamodb.DynamoDbRecoveryStrategyReadAdapter;
import br.com.creditrecovery.api.config.ApiProperties;
import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.CommunicationChannel;
import br.com.creditrecovery.domain.model.CustomerDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.util.List;
import java.util.Map;

import static br.com.creditrecovery.api.TestStrategies.GENERATED_AT;
import static br.com.creditrecovery.api.TestStrategies.VALID_CNPJ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoDbRecoveryStrategyReadRepositoryTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Test
    void shouldReadCurrentStrategyByPrimaryKey() {
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(GetItemResponse.builder()
                .item(item())
                .build());

        var repository = new DynamoDbRecoveryStrategyReadAdapter(
                dynamoDbClient,
                new ApiProperties("CreditRecoveryStrategy")
        );

        var result = repository.findCurrentByCnpj(CustomerDocument.of(VALID_CNPJ));

        assertThat(result).isPresent();
        assertThat(result.get().actions()).hasSize(1);
        assertThat(result.get().actions().getFirst().type()).isEqualTo(ActionType.NEGATIVACAO);

        ArgumentCaptor<GetItemRequest> captor = ArgumentCaptor.forClass(GetItemRequest.class);
        verify(dynamoDbClient).getItem(captor.capture());
        assertThat(captor.getValue().key().get("pk").s()).isEqualTo("CNPJ#" + VALID_CNPJ);
        assertThat(captor.getValue().key().get("sk").s()).isEqualTo("STRATEGY#CURRENT");
    }

    private Map<String, AttributeValue> item() {
        return Map.ofEntries(
                Map.entry("pk", s("CNPJ#" + VALID_CNPJ)),
                Map.entry("sk", s("STRATEGY#CURRENT")),
                Map.entry("cnpj", s(VALID_CNPJ)),
                Map.entry("riskLevel", s("HIGH")),
                Map.entry("daysOverdue", n(87)),
                Map.entry("debtAmount", n("125000.50")),
                Map.entry("productType", s("CREDIT_CARD_PJ")),
                Map.entry("score", n(812)),
                Map.entry("actions", AttributeValue.builder().l(List.of(action())).build()),
                Map.entry("partnerOffice", s("OFFICE_A")),
                Map.entry("strategyVersion", s("v1")),
                Map.entry("status", s("ACTIVE")),
                Map.entry("generatedAt", s(GENERATED_AT.toString()))
        );
    }

    private AttributeValue action() {
        return AttributeValue.builder()
                .m(Map.of(
                        "type", s(ActionType.NEGATIVACAO.name()),
                        "channel", s(CommunicationChannel.NONE.name()),
                        "priority", s(ActionPriority.HIGH.name()),
                        "reason", s("Cliente PJ com atraso superior a 60 dias")
                ))
                .build();
    }

    private AttributeValue s(String value) {
        return AttributeValue.builder().s(value).build();
    }

    private AttributeValue n(Number value) {
        return AttributeValue.builder().n(String.valueOf(value)).build();
    }

    private AttributeValue n(String value) {
        return AttributeValue.builder().n(value).build();
    }
}
