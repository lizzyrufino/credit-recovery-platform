package br.com.creditrecovery.api;

import br.com.creditrecovery.api.config.ApiProperties;
import br.com.creditrecovery.api.config.AwsClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AwsClientProperties.class, ApiProperties.class})
public class CreditStrategyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditStrategyApiApplication.class, args);
    }
}
