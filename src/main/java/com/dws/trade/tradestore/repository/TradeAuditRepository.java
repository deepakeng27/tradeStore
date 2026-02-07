package com.dws.trade.tradestore.repository;

import com.dws.trade.tradestore.model.TradeAudit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * MongoDB repository for TradeAudit document.
 */
@Repository
public interface TradeAuditRepository extends MongoRepository<TradeAudit, String> {

    /**
     * Find all audit records for a specific trade.
     * @param tradeId the trade ID
     * @return a list of audit records
     */
    List<TradeAudit> findByTradeId(String tradeId);
}
