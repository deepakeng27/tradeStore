package com.dws.trade.tradestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import lombok.extern.slf4j.Slf4j;

/**
 * Trade Store Application - Main entry point
 *
 * This application provides a comprehensive solution for managing financial trades with:
 * - Version control and maturity date validation
 * - Event streaming via Apache Kafka
 * - Dual database support (PostgreSQL + MongoDB)
 * - RESTful API with Swagger documentation
 * - Comprehensive testing and CI/CD pipeline
 */
@SpringBootApplication
@EnableKafka
@Slf4j
public class TradeStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradeStoreApplication.class, args);

		log.info("========================================");
		log.info("Trade Store Application started successfully!");
		log.info("API Documentation: http://localhost:8080/api/swagger-ui.html");
		log.info("========================================");
	}

}
