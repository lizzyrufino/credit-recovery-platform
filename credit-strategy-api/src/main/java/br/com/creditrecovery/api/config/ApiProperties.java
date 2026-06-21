package br.com.creditrecovery.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.api")
public record ApiProperties(String strategyTableName) {

    public ApiProperties {
        strategyTableName = strategyTableName == null || strategyTableName.isBlank()
                ? "CreditRecoveryStrategy"
                : strategyTableName;
    }
}
