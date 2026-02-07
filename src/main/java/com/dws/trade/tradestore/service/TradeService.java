package com.dws.trade.tradestore.service;

import com.dws.trade.tradestore.dto.TradeRequestDTO;
import com.dws.trade.tradestore.dto.TradeResponseDTO;
import com.dws.trade.tradestore.exception.TradeNotFoundException;
import com.dws.trade.tradestore.model.Trade;
import com.dws.trade.tradestore.model.TradeAudit;
import com.dws.trade.tradestore.model.TradeStatus;
import com.dws.trade.tradestore.repository.TradeAuditRepository;
import com.dws.trade.tradestore.repository.TradeRepository;
import com.dws.trade.tradestore.validator.TradeMaturityValidator;
import com.dws.trade.tradestore.validator.TradeVersionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for Trade operations.
 * Implements business logic for trade creation, updating, and validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TradeService {

    private final TradeRepository tradeRepository;
    private final TradeAuditRepository tradeAuditRepository;
    private final TradeVersionValidator versionValidator;
    private final TradeMaturityValidator maturityValidator;
    private final TradeProducerService tradeProducerService;

    /**
     * Create or update a trade based on version control rules.
     * @param requestDTO the trade request DTO
     * @return the saved trade as response DTO
     */
    public TradeResponseDTO saveTrade(TradeRequestDTO requestDTO) {
        log.info("Processing trade: {}", requestDTO.getTradeId());

        // Validate maturity date
        maturityValidator.validateMaturityDate(requestDTO.getMaturityDate(), requestDTO.getTradeId());

        // Check for existing trade
        Optional<Trade> existingTrade = tradeRepository.findByTradeId(requestDTO.getTradeId());

        // Validate version
        versionValidator.validateVersion(existingTrade, requestDTO.getVersion(), requestDTO.getTradeId());

        // Create or update trade
        Trade trade;
        String action;

        if (existingTrade.isPresent()) {
            Trade existing = existingTrade.get();

            // If same version, replace; if higher version, update
            trade = existing;
            trade.setVersion(requestDTO.getVersion());
            trade.setCounterPartyId(requestDTO.getCounterPartyId());
            trade.setBookId(requestDTO.getBookId());
            trade.setMaturityDate(requestDTO.getMaturityDate());
            trade.setStatus(TradeStatus.ACTIVE);
            trade.setExpired(false);

            action = "UPDATE";
            log.info("Updating trade: {}", requestDTO.getTradeId());
        } else {
            trade = Trade.builder()
                .tradeId(requestDTO.getTradeId())
                .version(requestDTO.getVersion())
                .counterPartyId(requestDTO.getCounterPartyId())
                .bookId(requestDTO.getBookId())
                .maturityDate(requestDTO.getMaturityDate())
                .status(TradeStatus.ACTIVE)
                .expired(false)
                .build();

            action = "CREATE";
            log.info("Creating new trade: {}", requestDTO.getTradeId());
        }

        // Save to PostgreSQL
        Trade savedTrade = tradeRepository.save(trade);

        // Audit to MongoDB
        createAudit(savedTrade, action, "Trade processed successfully");

        // Publish to Kafka
        tradeProducerService.sendTradeEvent(savedTrade, action);

        return mapToResponseDTO(savedTrade);
    }

    /**
     * Get a trade by its trade ID.
     * @param tradeId the trade ID
     * @return the trade as response DTO
     * @throws TradeNotFoundException if trade not found
     */
    public TradeResponseDTO getTradeByTradeId(String tradeId) {
        return tradeRepository.findByTradeId(tradeId)
            .map(this::mapToResponseDTO)
            .orElseThrow(() -> new TradeNotFoundException(
                String.format("Trade %s not found", tradeId)
            ));
    }

    /**
     * Get all trades.
     * @return list of all trades as response DTOs
     */
    public List<TradeResponseDTO> getAllTrades() {
        return tradeRepository.findAll()
            .stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Mark a trade as expired if its maturity date has passed.
     * @param tradeId the trade ID
     * @return the expired trade as response DTO
     */
    public TradeResponseDTO markTradeAsExpired(String tradeId) {
        Trade trade = tradeRepository.findByTradeId(tradeId)
            .orElseThrow(() -> new TradeNotFoundException(
                String.format("Trade %s not found", tradeId)
            ));

        trade.setExpired(true);
        trade.setStatus(TradeStatus.EXPIRED);
        trade.setExpiryDate(LocalDate.now());

        Trade savedTrade = tradeRepository.save(trade);
        createAudit(savedTrade, "EXPIRE", "Trade marked as expired");
        tradeProducerService.sendTradeEvent(savedTrade, "EXPIRE");

        log.info("Trade {} marked as expired", tradeId);
        return mapToResponseDTO(savedTrade);
    }

    /**
     * Get audit trail for a specific trade.
     * @param tradeId the trade ID
     * @return list of audit records
     */
    public List<TradeAudit> getAuditTrail(String tradeId) {
        return tradeAuditRepository.findByTradeId(tradeId);
    }

    /**
     * Create an audit record in MongoDB.
     */
    private void createAudit(Trade trade, String action, String reason) {
        TradeAudit audit = TradeAudit.builder()
            .tradeId(trade.getTradeId())
            .version(trade.getVersion())
            .counterPartyId(trade.getCounterPartyId())
            .bookId(trade.getBookId())
            .maturityDate(trade.getMaturityDate().toString())
            .createdDate(trade.getCreatedDate().toString())
            .action(action)
            .reason(reason)
            .auditTimestamp(LocalDateTime.now())
            .status(trade.getStatus().name())
            .build();

        tradeAuditRepository.save(audit);
        log.debug("Audit record created for trade: {}", trade.getTradeId());
    }

    /**
     * Map Trade entity to TradeResponseDTO.
     */
    private TradeResponseDTO mapToResponseDTO(Trade trade) {
        return TradeResponseDTO.builder()
            .id(trade.getId())
            .tradeId(trade.getTradeId())
            .version(trade.getVersion())
            .counterPartyId(trade.getCounterPartyId())
            .bookId(trade.getBookId())
            .maturityDate(trade.getMaturityDate())
            .createdDate(trade.getCreatedDate())
            .updatedDate(trade.getUpdatedDate())
            .expired(trade.getExpired())
            .expiryDate(trade.getExpiryDate())
            .status(trade.getStatus().name())
            .build();
    }
}
