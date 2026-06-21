package br.com.creditrecovery.processor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.processor")
public record ProcessorProperties(
        String strategyTableName,
        String queueUrl,
        boolean pollingEnabled,
        int maxMessages,
        int waitTimeSeconds
) {

    public ProcessorProperties {
        strategyTableName = strategyTableName == null || strategyTableName.isBlank()
                ? "CreditRecoveryStrategy"
                : strategyTableName;
        maxMessages = maxMessages <= 0 ? 5 : maxMessages;
        waitTimeSeconds = waitTimeSeconds < 0 ? 10 : waitTimeSeconds;
    }
}
