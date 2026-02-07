package com.dws.trade.tradestore.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Kafka configuration for Trade Store application.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.topic.trade-events:trade-events}")
    private String tradeEventsTopic;

    @Value("${kafka.topic.trade-updates:trade-updates}")
    private String tradeUpdatesTopic;


    /**
     * Create trade-events topic.
     */
    @Bean
    public NewTopic tradeEventsTopic() {
        return new NewTopic(tradeEventsTopic, 3, (short) 1)
            .configs(java.util.Map.of(
                "retention.ms", "604800000"  // 7 days
            ));
    }

    /**
     * Create trade-updates topic.
     */
    @Bean
    public NewTopic tradeUpdatesTopic() {
        return new NewTopic(tradeUpdatesTopic, 3, (short) 1)
            .configs(java.util.Map.of(
                "retention.ms", "604800000"  // 7 days
            ));
    }

    /**
     * ObjectMapper bean for JSON serialization.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
