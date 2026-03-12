package com.fk.relocation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;

@Configuration
@Profile("!test")
public class JpaConfig {

    @Primary
    @Bean
    JpaTransactionManager transactionManager() {
        return new JpaTransactionManager();
    }
}
