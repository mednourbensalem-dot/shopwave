package com.shopwave.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info().title("ShopWave E-commerce API").version("1.0.0")
                .description("Click **Authorize** and use one of the demo accounts:\n\n"
                    + "- Admin: `admin@shop.com` / `admin123`\n"
                    + "- Customer: `customer@shop.com` / `customer123`\n\n"
                    + "Catalog endpoints are public. Cart and orders need a login. `/api/admin/**` and product/category writes need ADMIN."))
            .components(new Components().addSecuritySchemes("basicAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
            .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
