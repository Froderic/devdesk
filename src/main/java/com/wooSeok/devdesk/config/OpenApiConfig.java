package com.wooSeok.devdesk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI devDeskOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DevDesk API")
                        .description("Developer issue tracking system API")
                        .version("1.0.0"));
    }
}