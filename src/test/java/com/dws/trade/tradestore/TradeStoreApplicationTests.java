package com.dws.trade.tradestore;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for Spring Boot application context loading.
 * This test requires a running PostgreSQL database configured via TestContainers.
 * It is disabled by default for unit test runs.
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Integration test - requires running database with TestContainers")
class TradeStoreApplicationTests {

	@Test
	void contextLoads() {
	}

}
