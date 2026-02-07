package com.dws.trade.tradestore;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base test configuration using TestContainers for integration tests.
 * Automatically starts PostgreSQL, MongoDB, and Kafka containers for testing.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class TradeStoreTestBase {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
        .withDatabaseName("tradestore")
        .withUsername("postgres")
        .withPassword("postgres");

    static MongoDBContainer mongodb = new MongoDBContainer(DockerImageName.parse("mongo:6.0"))
        .withEnv("MONGO_INITDB_ROOT_USERNAME", "mongo")
        .withEnv("MONGO_INITDB_ROOT_PASSWORD", "mongo");

    static GenericContainer<?> kafka = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
        .withExposedPorts(9092)
        .withEnv("KAFKA_ZOOKEEPER_CONNECT", "localhost:2181")
        .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092")
        .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT")
        .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT");

    static {
        // Wait for containers to be ready
        postgres.start();
        mongodb.start();
        kafka.start();

        // Set system properties for Spring to use TestContainers
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
        System.setProperty("spring.data.mongodb.uri",
            mongodb.getReplicaSetUrl("tradestore"));
        System.setProperty("spring.kafka.bootstrap-servers",
            kafka.getHost() + ":" + kafka.getMappedPort(9092));
    }
}
