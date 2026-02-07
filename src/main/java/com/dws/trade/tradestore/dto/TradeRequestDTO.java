package com.dws.trade.tradestore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for creating or updating a trade.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeRequestDTO {

    @NotBlank(message = "Trade ID is required")
    private String tradeId;

    @NotNull(message = "Version is required")
    @Min(value = 1, message = "Version must be at least 1")
    private Integer version;

    @NotBlank(message = "Counter-party ID is required")
    private String counterPartyId;

    @NotBlank(message = "Book ID is required")
    private String bookId;

    @NotNull(message = "Maturity date is required")
    @FutureOrPresent(message = "Maturity date must be today or in the future")
    private LocalDate maturityDate;
}
