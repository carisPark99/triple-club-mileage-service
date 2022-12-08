package com.triple.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
public class BeanConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
