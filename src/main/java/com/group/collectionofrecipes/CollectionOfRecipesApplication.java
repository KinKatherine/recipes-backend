package com.group.collectionofrecipes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CollectionOfRecipesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectionOfRecipesApplication.class, args);
    }

}
