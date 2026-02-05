package com.innowise.userservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "User Service API",
                version = "1.0",
                description = "API for managing users and their payment cards"
        ),
        servers = {
                @Server(
                        url="http://localhost:8083",
                        description = "Development Server"
                )
        }
)
public class SwaggerConfig {
}
