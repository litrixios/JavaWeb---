package com.bjfu.cms.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springdoc.core.models.GroupedOpenApi; // 1. 引入这个包
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // 保留你原有的全局信息配置
    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("北林学术投稿系统后端测试用API")
                        .version("1.0")
                        .description("基于 Vue + Spring Boot 3 + MyBatis 的课设")
                        .contact(new Contact().name("JavaWeb--").email("2662775049@qq.com")));
    }

    @Bean
    public GroupedOpenApi cmsApi() {
        return GroupedOpenApi.builder()
                .group("学术投稿接口")      // 组名，会在 Swagger 页面右上角的下拉框显示
                .pathsToMatch("/api/**")  // 只匹配 /api/ 开头的路径
                .build();
    }
}