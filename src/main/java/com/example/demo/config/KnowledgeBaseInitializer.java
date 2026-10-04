package com.example.demo.config;

import com.example.demo.service.KnowledgeBaseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        name = "knowledge-base.initializer.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class KnowledgeBaseInitializer {

    @Bean
    CommandLineRunner loadKnowledgeBase(
            KnowledgeBaseService knowledgeBaseService
    ) {

        return args -> {

            knowledgeBaseService.loadDocument(
                    "classpath:/knowledge/return-policy.txt"
            );

            knowledgeBaseService.loadDocument(
                    "classpath:/knowledge/refund-policy.txt"
            );

            knowledgeBaseService.loadDocument(
                    "classpath:/knowledge/shipping-policy.txt"
            );
        };
    }
}