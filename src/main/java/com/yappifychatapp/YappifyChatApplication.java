package com.yappifychatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class YappifyChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(YappifyChatApplication.class, args);
    }
}
