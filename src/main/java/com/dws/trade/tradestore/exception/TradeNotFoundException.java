package com.dws.trade.tradestore.exception;

/**
 * Exception thrown when a trade is not found.
 */
public class TradeNotFoundException extends RuntimeException {
    public TradeNotFoundException(String message) {
        super(message);
    }

    public TradeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
