package com.generation.farmacia.configuration;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;

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
                    .title("API da Loja de Jogos Carlos Games")
                    .description("Projeto de Loja de Jogos da Carlos Games")
                    .version("1.0")
                    .contact(new Contact()
                        .name("Carlos Games")
                        .url("https://github.com/carlosmoronisud") 
                        .email("carlosmoronisud@gmail.com")) 
                    .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                    .description("GitHub do Projeto")
                    .url("https://github.com/carlosmoronisud/lojaGamesBack")); 
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