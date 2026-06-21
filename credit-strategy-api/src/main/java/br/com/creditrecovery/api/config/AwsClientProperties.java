package br.com.creditrecovery.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws")
public record AwsClientProperties(
        String region,
        String endpointOverride,
        String accessKey,
        String secretKey
) {

    public AwsClientProperties {
        region = region == null || region.isBlank() ? "us-east-1" : region;
        accessKey = accessKey == null || accessKey.isBlank() ? "test" : accessKey;
        secretKey = secretKey == null || secretKey.isBlank() ? "test" : secretKey;
    }
}
