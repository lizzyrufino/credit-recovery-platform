package br.com.creditrecovery.api.adapter.out.dynamodb;

import br.com.creditrecovery.api.application.port.out.RecoveryStrategyLookupPort;
import br.com.creditrecovery.api.config.ApiProperties;
import br.com.creditrecovery.domain.enums.ActionPriority;
import br.com.creditrecovery.domain.enums.ActionType;
import br.com.creditrecovery.domain.enums.CommunicationChannel;
import br.com.creditrecovery.domain.enums.ProductType;
import br.com.creditrecovery.domain.enums.RiskLevel;
import br.com.creditrecovery.domain.enums.StrategyStatus;
import br.com.creditrecovery.domain.model.CustomerDocument;
import br.com.creditrecovery.domain.model.RecoveryAction;
import br.com.creditrecovery.domain.model.RecoveryStrategy;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DynamoDbRecoveryStrategyReadAdapter implements RecoveryStrategyLookupPort {

    private static final String PK = "pk";
    private static final String SK = "sk";
    private static final String CURRENT_SK = "STRATEGY#CURRENT";

    private final DynamoDbClient dynamoDbClient;
    private final ApiProperties properties;

    @Override
    @Retry(name = "dynamodb")
    @CircuitBreaker(name = "dynamodb")
    public Optional<RecoveryStrategy> findCurrentByCnpj(CustomerDocument document) {
        Map<String, AttributeValue> item = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(properties.strategyTableName())
                .key(Map.of(
                        PK, s("CNPJ#" + document.value()),
                        SK, s(CURRENT_SK)
                ))
                .consistentRead(false)
                .build()).item();

        return item == null || item.isEmpty() ? Optional.empty() : Optional.of(toDomain(item));
    }

    @Override
    @Retry(name = "dynamodb")
    @CircuitBreaker(name = "dynamodb")
    public List<RecoveryStrategy> findHistoryByCnpj(CustomerDocument document) {
        return dynamoDbClient.query(QueryRequest.builder()
                        .tableName(properties.strategyTableName())
                        .keyConditionExpression("pk = :pk and begins_with(sk, :skPrefix)")
                        .expressionAttributeValues(Map.of(
                                ":pk", s("CNPJ#" + document.value()),
                                ":skPrefix", s("STRATEGY#")
                        ))
                        .scanIndexForward(false)
                        .build())
                .items()
                .stream()
                .filter(item -> !CURRENT_SK.equals(item.get(SK).s()))
                .map(this::toDomain)
                .sorted(Comparator.comparing(RecoveryStrategy::generatedAt).reversed())
                .toList();
    }

    private RecoveryStrategy toDomain(Map<String, AttributeValue> item) {
        return new RecoveryStrategy(
                CustomerDocument.of(text(item, "cnpj")),
                RiskLevel.valueOf(text(item, "riskLevel")),
                integer(item, "daysOverdue"),
                decimal(item, "debtAmount"),
                ProductType.valueOf(text(item, "productType")),
                integer(item, "score"),
                actions(item.getOrDefault("actions", AttributeValue.builder().l(List.of()).build()).l()),
                text(item, "partnerOffice"),
                text(item, "strategyVersion"),
                StrategyStatus.valueOf(text(item, "status")),
                Instant.parse(text(item, "generatedAt"))
        );
    }

    private List<RecoveryAction> actions(List<AttributeValue> values) {
        return values.stream()
                .map(AttributeValue::m)
                .map(map -> new RecoveryAction(
                        ActionType.valueOf(text(map, "type")),
                        CommunicationChannel.valueOf(text(map, "channel")),
                        ActionPriority.valueOf(text(map, "priority")),
                        text(map, "reason")
                ))
                .toList();
    }

    private String text(Map<String, AttributeValue> item, String attribute) {
        AttributeValue value = item.get(attribute);
        return value == null || value.s() == null ? "" : value.s();
    }

    private int integer(Map<String, AttributeValue> item, String attribute) {
        AttributeValue value = item.get(attribute);
        return value == null || value.n() == null ? 0 : Integer.parseInt(value.n());
    }

    private BigDecimal decimal(Map<String, AttributeValue> item, String attribute) {
        AttributeValue value = item.get(attribute);
        return value == null || value.n() == null ? BigDecimal.ZERO : new BigDecimal(value.n());
    }

    private AttributeValue s(String value) {
        return AttributeValue.builder().s(value).build();
    }
}
