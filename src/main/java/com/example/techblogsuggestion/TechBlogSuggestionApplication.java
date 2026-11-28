package com.example.techblogsuggestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TechBlogSuggestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechBlogSuggestionApplication.class, args);
    }

}
