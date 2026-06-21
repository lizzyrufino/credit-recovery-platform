package br.com.creditrecovery.processor.adapter.out.dynamodb;

import br.com.creditrecovery.domain.event.CreditProfileReceivedEvent;
import br.com.creditrecovery.domain.model.RecoveryAction;
import br.com.creditrecovery.domain.model.RecoveryStrategy;
import br.com.creditrecovery.processor.application.port.out.RecoveryStrategyPersistencePort;
import br.com.creditrecovery.processor.config.ProcessorProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DynamoDbRecoveryStrategyAdapter implements RecoveryStrategyPersistencePort {

    private static final String PK = "pk";
    private static final String SK = "sk";
    private static final String IDEMPOTENCY_SK = "IDEMPOTENCY";

    private final DynamoDbClient dynamoDbClient;
    private final ProcessorProperties properties;

    @Override
    @Retry(name = "dynamodb")
    @CircuitBreaker(name = "dynamodb")
    public boolean isEventProcessed(String eventId) {
        Map<String, AttributeValue> item = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(properties.strategyTableName())
                .key(Map.of(
                        PK, s("EVENT#" + eventId),
                        SK, s(IDEMPOTENCY_SK)
                ))
                .consistentRead(true)
                .build()).item();

        return item != null && !item.isEmpty();
    }

    @Override
    @Retry(name = "dynamodb")
    @CircuitBreaker(name = "dynamodb")
    public void saveStrategyAndMarkEvent(RecoveryStrategy strategy, CreditProfileReceivedEvent event) {
        Map<String, AttributeValue> currentItem = toItem(strategy, "STRATEGY#CURRENT");
        Map<String, AttributeValue> historicalItem = toItem(strategy, "STRATEGY#" + strategy.generatedAt());
        Map<String, AttributeValue> eventItem = eventItem(event, strategy);

        try {
            dynamoDbClient.transactWriteItems(TransactWriteItemsRequest.builder()
                    .transactItems(List.of(
                            TransactWriteItem.builder()
                                    .put(Put.builder().tableName(properties.strategyTableName()).item(currentItem).build())
                                    .build(),
                            TransactWriteItem.builder()
                                    .put(Put.builder().tableName(properties.strategyTableName()).item(historicalItem).build())
                                    .build(),
                            TransactWriteItem.builder()
                                    .put(Put.builder()
                                            .tableName(properties.strategyTableName())
                                            .item(eventItem)
                                            .conditionExpression("attribute_not_exists(pk)")
                                            .build())
                                    .build()
                    ))
                    .build());
        } catch (TransactionCanceledException exception) {
            if (exception.cancellationReasons().stream()
                    .anyMatch(reason -> "ConditionalCheckFailed".equals(reason.code()))) {
                throw new AlreadyProcessedEventException(event.eventId());
            }
            throw exception;
        }
    }

    private Map<String, AttributeValue> toItem(RecoveryStrategy strategy, String sk) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put(PK, s("CNPJ#" + strategy.cnpj()));
        item.put(SK, s(sk));
        item.put("cnpj", s(strategy.cnpj()));
        item.put("riskLevel", s(strategy.riskLevel().name()));
        item.put("daysOverdue", n(strategy.daysOverdue()));
        item.put("debtAmount", n(strategy.debtAmount()));
        item.put("productType", s(strategy.productType().name()));
        item.put("recommendedActions", l(strategy.actions().stream()
                .map(action -> s(action.type().name()))
                .toList()));
        item.put("actions", l(strategy.actions().stream()
                .map(this::action)
                .toList()));
        item.put("partnerOffice", s(strategy.partnerOffice()));
        item.put("score", n(strategy.score()));
        item.put("strategyVersion", s(strategy.strategyVersion()));
        item.put("status", s(strategy.status().name()));
        item.put("generatedAt", s(strategy.generatedAt().toString()));
        return item;
    }

    private Map<String, AttributeValue> eventItem(CreditProfileReceivedEvent event, RecoveryStrategy strategy) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put(PK, s("EVENT#" + event.eventId()));
        item.put(SK, s(IDEMPOTENCY_SK));
        item.put("eventId", s(event.eventId()));
        item.put("correlationId", s(event.correlationId()));
        item.put("occurredAt", s(event.occurredAt().toString()));
        item.put("processedAt", s(strategy.generatedAt().toString()));
        item.put("customerHash", s(strategy.document().hashed()));
        item.put("status", s("PROCESSED"));
        return item;
    }

    private AttributeValue action(RecoveryAction action) {
        return AttributeValue.builder()
                .m(Map.of(
                        "type", s(action.type().name()),
                        "channel", s(action.channel().name()),
                        "priority", s(action.priority().name()),
                        "reason", s(action.reason())
                ))
                .build();
    }

    private AttributeValue s(String value) {
        return AttributeValue.builder().s(value == null ? "" : value).build();
    }

    private AttributeValue n(Number value) {
        return AttributeValue.builder().n(String.valueOf(value)).build();
    }

    private AttributeValue l(List<AttributeValue> values) {
        return AttributeValue.builder().l(values).build();
    }
}
