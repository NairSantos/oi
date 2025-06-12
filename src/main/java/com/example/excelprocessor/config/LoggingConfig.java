package com.example.excelprocessor.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class LoggingConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);
    
    @Bean
    public CommandLineRunner logStartup() {
        return args -> {
            logger.info("============================================");
            logger.info("Aplicação de Processamento Excel iniciada!");
            logger.info("============================================");
            logger.info("Acesse: http://localhost:8085");
            logger.info("A aplicação está configurada para processar:");
            logger.info("- Planilhas JIRA com sheet 'Usuários'");
            logger.info("- Planilhas CLAIM com sheet 'ClaimBase'");
            logger.info("============================================");
        };
    }
}