package com.dws.trade.tradestore.model;

/**
 * Enum representing the status of a trade.
 */
public enum TradeStatus {
    ACTIVE("Trade is active and not expired"),
    EXPIRED("Trade has expired"),
    REJECTED("Trade has been rejected");

    private final String description;

    TradeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
