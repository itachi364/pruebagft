package com.example.s3renaming.infrastructure.config;

import com.example.s3renaming.domain.DateNormalizer;
import com.example.s3renaming.domain.RuleEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    DateNormalizer dateNormalizer() {
        return new DateNormalizer();
    }

    @Bean
    RuleEngine ruleEngine(DateNormalizer dateNormalizer) {
        return new RuleEngine(dateNormalizer);
    }
}

