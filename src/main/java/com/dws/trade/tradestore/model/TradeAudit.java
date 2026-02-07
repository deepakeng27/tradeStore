package com.dws.trade.tradestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * MongoDB document for storing trade audit trail and history.
 * This provides an immutable record of all trade events and changes.
 */
@Document(collection = "trade_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeAudit {

    @Id
    private String id;

    private String tradeId;

    private Integer version;

    private String counterPartyId;

    private String bookId;

    private String maturityDate;

    private String createdDate;

    private String action;  // CREATE, UPDATE, REJECT, EXPIRE

    private String reason;

    private LocalDateTime auditTimestamp;

    private String status;  // ACTIVE, EXPIRED, REJECTED
}
