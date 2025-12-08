package com.maxed.app;

import com.maxed.app.jwt.JwtConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication

@ComponentScan(basePackages = {
        "com.maxed.app",
        "com.maxed.userservice.impl",
        "com.maxed.chatservice.impl",
        "com.maxed.mediaservice.impl",
        "com.maxed.searchservice",
        "com.maxed.common.util"
})

@EnableJpaRepositories(basePackages = {
        "com.maxed.userservice.impl",
        "com.maxed.chatservice.impl"
})

@EnableElasticsearchRepositories(basePackages = "com.maxed.searchservice")

@EntityScan(basePackages = {
        "com.maxed.userservice.impl",
        "com.maxed.chatservice.impl"
})

@EnableConfigurationProperties(JwtConfig.class)
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
