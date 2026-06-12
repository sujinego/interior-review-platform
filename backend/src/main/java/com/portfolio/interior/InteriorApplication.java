package com.portfolio.interior;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class InteriorApplication {

    public static void main(String[] args) {
        SpringApplication.run(InteriorApplication.class, args);
    }
}
