package com.dws.trade.tradestore.service;

import com.dws.trade.tradestore.model.Trade;
import com.dws.trade.tradestore.model.TradeAudit;
import com.dws.trade.tradestore.model.TradeStatus;
import com.dws.trade.tradestore.repository.TradeRepository;
import com.dws.trade.tradestore.repository.TradeAuditRepository;
import com.dws.trade.tradestore.validator.TradeMaturityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for scheduled tasks related to trades.
 * Automatically marks trades as expired when their maturity date passes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
@Transactional
public class TradeSchedulerService {

    private final TradeRepository tradeRepository;
    private final TradeMaturityValidator maturityValidator;
    private final TradeAuditRepository tradeAuditRepository;

    /**
     * Scheduled task to mark expired trades.
     * Runs daily at 00:00 (midnight).
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void markExpiredTrades() {
        log.info("Starting scheduled task to mark expired trades");

        try {
            // Find all non-expired active trades
            List<Trade> activeTrades = tradeRepository.findByExpiredFalse();

            int expiredCount = 0;
            LocalDate today = LocalDate.now();

            for (Trade trade : activeTrades) {
                // Check if maturity date has passed
                if (maturityValidator.isExpired(trade.getMaturityDate())) {
                    log.info("Marking trade {} as expired. Maturity date: {}",
                        trade.getTradeId(), trade.getMaturityDate());

                    // Mark as expired
                    trade.setExpired(true);
                    trade.setStatus(TradeStatus.EXPIRED);
                    trade.setExpiryDate(today);
                    trade.setUpdatedDate(LocalDateTime.now());

                    // Save to database
                    tradeRepository.save(trade);

                    // Create audit record
                    createExpirationAudit(trade);

                    expiredCount++;
                }
            }

            log.info("Marked {} trades as expired", expiredCount);
        } catch (Exception e) {
            log.error("Error during scheduled expiration task", e);
        }
    }

    /**
     * Create audit record for trade expiration.
     */
    private void createExpirationAudit(Trade trade) {
        TradeAudit audit = TradeAudit.builder()
            .tradeId(trade.getTradeId())
            .version(trade.getVersion())
            .counterPartyId(trade.getCounterPartyId())
            .bookId(trade.getBookId())
            .maturityDate(trade.getMaturityDate().toString())
            .createdDate(trade.getCreatedDate().toString())
            .action("EXPIRE")
            .reason("Maturity date has passed - automatically marked as expired")
            .auditTimestamp(LocalDateTime.now())
            .status(TradeStatus.EXPIRED.name())
            .build();

        tradeAuditRepository.save(audit);
    }
}
