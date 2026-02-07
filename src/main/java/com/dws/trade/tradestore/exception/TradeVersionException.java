package com.dws.trade.tradestore.exception;

/**
 * Exception thrown when a trade with a lower version is received.
 */
public class TradeVersionException extends RuntimeException {
    public TradeVersionException(String message) {
        super(message);
    }

    public TradeVersionException(String message, Throwable cause) {
        super(message, cause);
    }
}
