package com.dws.trade.tradestore.exception;

/**
 * Exception thrown when a trade has an invalid maturity date.
 */
public class TradeMaturityException extends RuntimeException {
    public TradeMaturityException(String message) {
        super(message);
    }

    public TradeMaturityException(String message, Throwable cause) {
        super(message, cause);
    }
}
