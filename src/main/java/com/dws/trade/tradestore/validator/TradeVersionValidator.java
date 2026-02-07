package com.dws.trade.tradestore.validator;

import com.dws.trade.tradestore.exception.TradeVersionException;
import com.dws.trade.tradestore.model.Trade;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Validator for trade version control logic.
 * Implements rule: reject lower versions, accept same versions (replace), reject higher versions.
 */
@Component
public class TradeVersionValidator {

    /**
     * Validates the version of an incoming trade against an existing trade.
     * @param existingTrade the existing trade in the store
     * @param incomingVersion the version of the incoming trade
     * @param tradeId the trade ID for error reporting
     * @throws TradeVersionException if the version validation fails
     */
    public void validateVersion(Optional<Trade> existingTrade, Integer incomingVersion, String tradeId) {
        if (existingTrade.isPresent()) {
            Trade existing = existingTrade.get();
            if (incomingVersion < existing.getVersion()) {
                throw new TradeVersionException(
                    String.format("Trade %s rejected: incoming version %d is lower than existing version %d",
                        tradeId, incomingVersion, existing.getVersion())
                );
            }
            // Same version or higher version are allowed - will be handled by service logic
        }
    }
}
