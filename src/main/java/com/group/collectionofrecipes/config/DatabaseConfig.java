package com.group.collectionofrecipes.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@Slf4j
public class DatabaseConfig {


    @EventListener(ApplicationReadyEvent.class)
    public void checkDatabaseConnection() {
        try {
            log.info("Spring Data JPA + Hibernate connection successful");
        } catch (Exception e) {
            log.error("JPA+Hibernate connection failed", e);
            log.error("Application will now exit");
            System.exit(1);
        }
    }
}