package com.maxed.app;

import com.maxed.app.jwt.JwtConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.maxed.app", "com.maxed.userservice.impl"})
@EnableJpaRepositories(basePackages = "com.maxed.userservice.impl")
@EntityScan(basePackages = "com.maxed.userservice.impl")
@EnableConfigurationProperties(JwtConfig.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
