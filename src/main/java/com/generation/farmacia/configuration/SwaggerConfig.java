package com.generation.farmacia.configuration;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI; // ATENÇÃO: OpenAPI com "API" maiúsculas
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                    .title("API da Farmacia E-commerce") // Ajuste o título
                    .description("Projeto de E-commerce de Farmácia") // Ajuste a descrição
                    .version("1.0")
                    .contact(new Contact()
                        .name("Seu Nome Aqui") // SEU NOME
                        .url("https://github.com/carlosmoronisud") // Seu GitHub
                        .email("seu.email@example.com")) // Seu E-mail
                    .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                    .description("GitHub do Projeto Farmacia") // Ajuste a descrição
                    .url("https://github.com/carlosmoronisud/projeto_final_bloco_02")); // Seu repositório do projeto
    }

    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomiser() {
        return openApi -> {
            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                ApiResponses apiResponses = operation.getResponses();
                apiResponses.addApiResponse("200", new ApiResponse().description("Sucesso!"));
                apiResponses.addApiResponse("201", new ApiResponse().description("Criado!"));
                apiResponses.addApiResponse("204", new ApiResponse().description("Objeto Excluído!"));
                apiResponses.addApiResponse("400", new ApiResponse().description("Erro na Requisição!"));
                apiResponses.addApiResponse("401", new ApiResponse().description("Não Autorizado!"));
                apiResponses.addApiResponse("403", new ApiResponse().description("Acesso Proibido!"));
                apiResponses.addApiResponse("404", new ApiResponse().description("Objeto Não Encontrado!"));
                apiResponses.addApiResponse("500", new ApiResponse().description("Erro na Aplicação!"));
            }));
        };
    }
}