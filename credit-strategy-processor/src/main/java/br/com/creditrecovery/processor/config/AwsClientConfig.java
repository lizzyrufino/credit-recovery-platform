package br.com.creditrecovery.processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;
import java.time.Clock;

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

    @Bean
    SqsClient sqsClient(AwsClientProperties properties) {
        var builder = SqsClient.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider(properties));

        if (properties.endpointOverride() != null && !properties.endpointOverride().isBlank()) {
            builder.endpointOverride(URI.create(properties.endpointOverride()));
        }

        return builder.build();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    private AwsCredentialsProvider credentialsProvider(AwsClientProperties properties) {
        if (properties.endpointOverride() != null && !properties.endpointOverride().isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.accessKey(), properties.secretKey()));
        }
        return DefaultCredentialsProvider.create();
    }
}
