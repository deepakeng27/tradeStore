package com.dws.trade.tradestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Trade entity representing a financial trade record in PostgreSQL.
 * This is the primary transactional entity with version control and audit fields.
 */
@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Trade ID cannot be blank")
    private String tradeId;

    @Column(nullable = false)
    @NotNull(message = "Version cannot be null")
    @Min(value = 1, message = "Version must be at least 1")
    private Integer version;

    @Column(nullable = false)
    @NotBlank(message = "Counter-party ID cannot be blank")
    private String counterPartyId;

    @Column(nullable = false)
    @NotBlank(message = "Book ID cannot be blank")
    private String bookId;

    @Column(nullable = false)
    @NotNull(message = "Maturity date cannot be null")
    @FutureOrPresent(message = "Maturity date must be in the future or today")
    private LocalDate maturityDate;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "expired")
    @Builder.Default
    private Boolean expired = false;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TradeStatus status = TradeStatus.ACTIVE;

    /**
     * Pre-persist hook to set creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    /**
     * Pre-update hook to update the timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
