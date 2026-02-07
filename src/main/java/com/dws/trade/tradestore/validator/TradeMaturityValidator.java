package com.dws.trade.tradestore.validator;

import com.dws.trade.tradestore.exception.TradeMaturityException;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

/**
 * Validator for trade maturity date logic.
 * Implements rule: reject trades with maturity date before today.
 */
@Component
public class TradeMaturityValidator {

    /**
     * Validates that the maturity date is not in the past.
     * @param maturityDate the maturity date to validate
     * @param tradeId the trade ID for error reporting
     * @throws TradeMaturityException if maturity date is before today
     */
    public void validateMaturityDate(LocalDate maturityDate, String tradeId) {
        LocalDate today = LocalDate.now();
        if (maturityDate.isBefore(today)) {
            throw new TradeMaturityException(
                String.format("Trade %s rejected: maturity date %s is before today's date %s",
                    tradeId, maturityDate, today)
            );
        }
    }

    /**
     * Check if a trade should be marked as expired.
     * A trade is expired if its maturity date has passed.
     * @param maturityDate the maturity date of the trade
     * @return true if the trade should be marked as expired
     */
    public boolean isExpired(LocalDate maturityDate) {
        return maturityDate.isBefore(LocalDate.now());
    }
}
