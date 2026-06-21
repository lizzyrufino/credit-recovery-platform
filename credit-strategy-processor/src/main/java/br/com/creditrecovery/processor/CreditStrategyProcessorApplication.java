package br.com.creditrecovery.processor;

import br.com.creditrecovery.processor.config.AwsClientProperties;
import br.com.creditrecovery.processor.config.ProcessorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({AwsClientProperties.class, ProcessorProperties.class})
public class CreditStrategyProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditStrategyProcessorApplication.class, args);
    }
}
