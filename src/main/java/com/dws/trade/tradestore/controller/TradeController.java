package com.dws.trade.tradestore.controller;

import com.dws.trade.tradestore.dto.TradeRequestDTO;
import com.dws.trade.tradestore.dto.TradeResponseDTO;
import com.dws.trade.tradestore.model.TradeAudit;
import com.dws.trade.tradestore.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller for Trade operations.
 * Provides endpoints for creating, retrieving, and managing trades.
 */
@RestController
@RequestMapping("/trades")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trade Management", description = "APIs for managing trades in the store")
public class TradeController {

    private final TradeService tradeService;

    /**
     * Create or update a trade.
     * @param requestDTO the trade request DTO
     * @return the saved trade response DTO
     */
    @PostMapping
    @Operation(summary = "Create or update a trade",
               description = "Creates a new trade or updates an existing one based on version control rules")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Trade created successfully",
                     content = @Content(schema = @Schema(implementation = TradeResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid trade data"),
        @ApiResponse(responseCode = "409", description = "Trade version conflict")
    })
    public ResponseEntity<TradeResponseDTO> saveTrade(@Valid @RequestBody TradeRequestDTO requestDTO) {
        log.info("POST request to save trade: {}", requestDTO.getTradeId());
        TradeResponseDTO response = tradeService.saveTrade(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get a trade by trade ID.
     * @param tradeId the trade ID
     * @return the trade response DTO
     */
    @GetMapping("/{tradeId}")
    @Operation(summary = "Get a trade by ID",
               description = "Retrieves a specific trade by its trade ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade found",
                     content = @Content(schema = @Schema(implementation = TradeResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Trade not found")
    })
    public ResponseEntity<TradeResponseDTO> getTradeByTradeId(@PathVariable String tradeId) {
        log.info("GET request for trade: {}", tradeId);
        TradeResponseDTO response = tradeService.getTradeByTradeId(tradeId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all trades.
     * @return list of all trades
     */
    @GetMapping
    @Operation(summary = "Get all trades",
               description = "Retrieves all trades from the store")
    @ApiResponse(responseCode = "200", description = "List of trades retrieved",
                 content = @Content(schema = @Schema(implementation = TradeResponseDTO.class)))
    public ResponseEntity<List<TradeResponseDTO>> getAllTrades() {
        log.info("GET request for all trades");
        List<TradeResponseDTO> trades = tradeService.getAllTrades();
        return ResponseEntity.ok(trades);
    }

    /**
     * Mark a trade as expired.
     * @param tradeId the trade ID
     * @return the expired trade response DTO
     */
    @PutMapping("/{tradeId}/expire")
    @Operation(summary = "Mark a trade as expired",
               description = "Marks a trade as expired when its maturity date has passed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade marked as expired",
                     content = @Content(schema = @Schema(implementation = TradeResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Trade not found")
    })
    public ResponseEntity<TradeResponseDTO> markTradeAsExpired(@PathVariable String tradeId) {
        log.info("PUT request to expire trade: {}", tradeId);
        TradeResponseDTO response = tradeService.markTradeAsExpired(tradeId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get audit trail for a trade.
     * @param tradeId the trade ID
     * @return list of audit records
     */
    @GetMapping("/{tradeId}/audit")
    @Operation(summary = "Get audit trail for a trade",
               description = "Retrieves the complete audit trail (history) of a trade from MongoDB")
    @ApiResponse(responseCode = "200", description = "Audit trail retrieved",
                 content = @Content(schema = @Schema(implementation = TradeAudit.class)))
    public ResponseEntity<List<TradeAudit>> getAuditTrail(@PathVariable String tradeId) {
        log.info("GET request for audit trail of trade: {}", tradeId);
        List<TradeAudit> auditTrail = tradeService.getAuditTrail(tradeId);
        return ResponseEntity.ok(auditTrail);
    }
}
