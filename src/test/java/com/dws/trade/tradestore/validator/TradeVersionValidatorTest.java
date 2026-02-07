package com.dws.trade.tradestore.validator;

import com.dws.trade.tradestore.exception.TradeVersionException;
import com.dws.trade.tradestore.model.Trade;
import com.dws.trade.tradestore.model.TradeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TradeVersionValidator.
 */
class TradeVersionValidatorTest {

    private TradeVersionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TradeVersionValidator();
    }

    @Test
    void testValidateVersion_NoExistingTrade_ShouldPass() {
        assertDoesNotThrow(() -> validator.validateVersion(Optional.empty(), 1, "T1"));
    }

    @Test
    void testValidateVersion_SameVersion_ShouldPass() {
        Trade existingTrade = Trade.builder()
            .tradeId("T1")
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().plusDays(10))
            .createdDate(LocalDateTime.now())
            .status(TradeStatus.ACTIVE)
            .build();

        assertDoesNotThrow(() -> validator.validateVersion(Optional.of(existingTrade), 1, "T1"));
    }

    @Test
    void testValidateVersion_HigherVersion_ShouldPass() {
        Trade existingTrade = Trade.builder()
            .tradeId("T1")
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().plusDays(10))
            .createdDate(LocalDateTime.now())
            .status(TradeStatus.ACTIVE)
            .build();

        assertDoesNotThrow(() -> validator.validateVersion(Optional.of(existingTrade), 2, "T1"));
    }

    @Test
    void testValidateVersion_LowerVersion_ShouldThrow() {
        Trade existingTrade = Trade.builder()
            .tradeId("T1")
            .version(2)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().plusDays(10))
            .createdDate(LocalDateTime.now())
            .status(TradeStatus.ACTIVE)
            .build();

        assertThrows(TradeVersionException.class,
            () -> validator.validateVersion(Optional.of(existingTrade), 1, "T1"));
    }

    @Test
    void testValidateVersion_LowerVersionErrorMessage() {
        Trade existingTrade = Trade.builder()
            .tradeId("T1")
            .version(2)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().plusDays(10))
            .createdDate(LocalDateTime.now())
            .status(TradeStatus.ACTIVE)
            .build();

        TradeVersionException exception = assertThrows(TradeVersionException.class,
            () -> validator.validateVersion(Optional.of(existingTrade), 1, "T1"));

        assertTrue(exception.getMessage().contains("rejected"));
        assertTrue(exception.getMessage().contains("lower"));
    }
}
