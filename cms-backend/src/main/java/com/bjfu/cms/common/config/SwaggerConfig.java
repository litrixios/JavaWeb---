package com.bjfu.cms.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("北林学术投稿系统后端测试用API")
                        .version("1.0")
                        .description("基于 Vue + Spring Boot 3 + MyBatis 的课设")
                        .contact(new Contact().name("JavaWeb--").email("2662775049@qq.com")));

    }
}
