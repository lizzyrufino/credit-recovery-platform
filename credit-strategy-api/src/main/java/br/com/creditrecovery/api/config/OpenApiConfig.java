package br.com.creditrecovery.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI creditRecoveryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Credit Recovery Strategy API")
                        .version("v1")
                        .description("Consulta estratégias materializadas de recuperação de crédito para clientes PJ."));
    }
}
