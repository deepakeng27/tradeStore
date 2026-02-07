package com.dws.trade.tradestore.repository;

import com.dws.trade.tradestore.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for Trade entity (PostgreSQL).
 */
@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    /**
     * Find a trade by its unique trade ID.
     * @param tradeId the trade ID
     * @return an Optional containing the trade if found
     */
    Optional<Trade> findByTradeId(String tradeId);

    /**
     * Find all trades that are not expired.
     * @return a list of non-expired trades
     */
    List<Trade> findByExpiredFalse();
}
