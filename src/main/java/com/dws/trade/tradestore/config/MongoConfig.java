package com.dws.trade.tradestore.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB configuration to explicitly set connection parameters.
 * This ensures the MongoDB client connects to the correct host in Docker environments.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.dws.trade.tradestore.repository")
public class MongoConfig {

    @Value("${spring.data.mongodb.uri:mongodb://mongo:mongo@mongodb:27017/tradestore?authSource=admin}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, "tradestore");
    }
}
