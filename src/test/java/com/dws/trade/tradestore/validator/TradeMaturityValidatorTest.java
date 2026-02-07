package com.dws.trade.tradestore.validator;

import com.dws.trade.tradestore.exception.TradeMaturityException;
import com.dws.trade.tradestore.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TradeMaturityValidator.
 */
class TradeMaturityValidatorTest {

    private TradeMaturityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TradeMaturityValidator();
    }

    @Test
    void testValidateMaturityDate_FutureDate_ShouldPass() {
        LocalDate futureDate = LocalDate.now().plusDays(10);

        assertDoesNotThrow(() -> validator.validateMaturityDate(futureDate, "T1"));
    }

    @Test
    void testValidateMaturityDate_TodayDate_ShouldPass() {
        LocalDate today = LocalDate.now();

        assertDoesNotThrow(() -> validator.validateMaturityDate(today, "T1"));
    }

    @Test
    void testValidateMaturityDate_PastDate_ShouldThrow() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        assertThrows(TradeMaturityException.class,
            () -> validator.validateMaturityDate(pastDate, "T1"));
    }

    @Test
    void testIsExpired_PastDate_ShouldReturnTrue() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        assertTrue(validator.isExpired(pastDate));
    }

    @Test
    void testIsExpired_FutureDate_ShouldReturnFalse() {
        LocalDate futureDate = LocalDate.now().plusDays(10);

        assertFalse(validator.isExpired(futureDate));
    }

    @Test
    void testIsExpired_TodayDate_ShouldReturnFalse() {
        LocalDate today = LocalDate.now();

        assertFalse(validator.isExpired(today));
    }
}
