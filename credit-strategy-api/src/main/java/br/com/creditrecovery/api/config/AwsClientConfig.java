package br.com.creditrecovery.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class AwsClientConfig {

    @Bean
    DynamoDbClient dynamoDbClient(AwsClientProperties properties) {
        var builder = DynamoDbClient.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider(properties));

        if (properties.endpointOverride() != null && !properties.endpointOverride().isBlank()) {
            builder.endpointOverride(URI.create(properties.endpointOverride()));
        }

        return builder.build();
    }

    private AwsCredentialsProvider credentialsProvider(AwsClientProperties properties) {
        if (properties.endpointOverride() != null && !properties.endpointOverride().isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.accessKey(), properties.secretKey()));
        }
        return DefaultCredentialsProvider.create();
    }
}
