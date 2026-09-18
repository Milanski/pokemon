package com.mbls.challenge.pokemon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PokemonCollectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokemonCollectionApplication.class, args);
    }
}
