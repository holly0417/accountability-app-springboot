package com.github.holly.accountability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@EnableConfigurationProperties
@EnableAsync
@EnableCaching
@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication

public class AccountabilityApplication {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(AccountabilityApplication.class, args);
    }
}



