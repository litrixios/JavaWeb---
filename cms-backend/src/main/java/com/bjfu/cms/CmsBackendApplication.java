package com.bjfu.cms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.bjfu.cms.mapper") // <--mapper 文件都在这
@EnableAsync // <--开启异步任务支持
public class CmsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CmsBackendApplication.class, args);
	}

}
