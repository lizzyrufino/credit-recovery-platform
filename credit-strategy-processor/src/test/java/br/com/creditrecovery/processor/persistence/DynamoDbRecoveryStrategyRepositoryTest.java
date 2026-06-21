package br.com.creditrecovery.processor.persistence;

import br.com.creditrecovery.domain.event.CreditProfileReceivedEvent;
import br.com.creditrecovery.domain.enums.RiskLevel;
import br.com.creditrecovery.processor.TestProfiles;
import br.com.creditrecovery.processor.adapter.out.dynamodb.DynamoDbRecoveryStrategyAdapter;
import br.com.creditrecovery.processor.application.service.RecoveryStrategyEngine;
import br.com.creditrecovery.processor.config.ProcessorProperties;
import br.com.creditrecovery.processor.rules.NegativacaoOverdueRule;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static br.com.creditrecovery.processor.TestProfiles.delayedHistory;
import static br.com.creditrecovery.processor.TestProfiles.profile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamoDbRecoveryStrategyRepositoryTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    @Test
    void shouldDetectProcessedEventByPrimaryKey() {
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(GetItemResponse.builder()
                .item(Map.of("pk", AttributeValue.builder().s("EVENT#evt-1").build()))
                .build());

        var repository = repository();

        assertThat(repository.isEventProcessed("evt-1")).isTrue();
    }

    @Test
    void shouldPersistCurrentHistoryAndIdempotencyMarkerInSingleTransaction() {
        var strategy = new RecoveryStrategyEngine(
                List.of(new NegativacaoOverdueRule()),
                new SimpleMeterRegistry(),
                Clock.fixed(Instant.parse("2026-06-20T10:00:00Z"), ZoneOffset.UTC)
        ).generate(profile(87, RiskLevel.HIGH, true, true, delayedHistory()));
        var event = new CreditProfileReceivedEvent(
                "evt-1",
                "corr-1",
                Instant.parse("2026-06-20T09:59:00Z"),
                TestProfiles.profile(87, RiskLevel.HIGH, true, true, delayedHistory())
        );

        repository().saveStrategyAndMarkEvent(strategy, event);

        ArgumentCaptor<TransactWriteItemsRequest> captor = ArgumentCaptor.forClass(TransactWriteItemsRequest.class);
        verify(dynamoDbClient).transactWriteItems(captor.capture());
        assertThat(captor.getValue().transactItems()).hasSize(3);
    }

    private DynamoDbRecoveryStrategyAdapter repository() {
        return new DynamoDbRecoveryStrategyAdapter(
                dynamoDbClient,
                new ProcessorProperties("CreditRecoveryStrategy", "queue", true, 1, 0)
        );
    }
}
