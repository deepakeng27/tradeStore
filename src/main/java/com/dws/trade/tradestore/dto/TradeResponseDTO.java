package com.dws.trade.tradestore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for returning trade information in API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeResponseDTO {

    private Long id;

    private String tradeId;

    private Integer version;

    private String counterPartyId;

    private String bookId;

    private LocalDate maturityDate;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private Boolean expired;

    private LocalDate expiryDate;

    private String status;
}
