package br.com.desafio_votacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development server")
                ))
                .info(new Info()
                        .title("API de Votação")
                        .version("1.0")
                        .description("API para Gerenciamento de Pautas e Votação em Assembleias. " +
                                    "Esta API permite criar pautas, cadastrar associados, abrir sessões de votação e registrar votos.")
                        .termsOfService("http://swagger.io/terms/")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org"))
                        .contact(new Contact()
                                .name("Vinícius de Moraes")
                                .url("https://github.com/viniciusdemoraess/desafio-votacao-fullstack")
                                .email("viniciusdemoraespro@gmail.com")));
    }
}
