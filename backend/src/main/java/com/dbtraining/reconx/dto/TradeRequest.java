package com.dbtraining.reconx.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ============================================================================
 * TICKET-ADV053 — TradeRequest DTO (POST body)
 * TICKET-ADV029 — JSR-380 validation annotations live on the DTO, not the entity
 *
 * WHY:    Putting @Pattern/@Positive/@NotNull on the JPA entity couples
 *         persistence to wire format. The DTO is the wire contract; validate
 *         it before mapping.
 * ============================================================================
 */
public record TradeRequest(
        @NotBlank(message = "tradeRef is required")
        @Pattern(
                regexp = "^[A-Z]{3}-\\d{8}-\\d{4}$",
                message = "tradeRef must match AAA-YYYYMMDD-NNNN"
        )
        String tradeRef,

        @NotNull(message = "instrumentId is required")
        Long instrumentId,

        @NotNull(message = "counterpartyId is required")
        Long counterpartyId,

        @NotBlank(message = "assetClass is required")
        String assetClass,

        @NotBlank(message = "side is required")
        @Pattern(
                regexp = "^(BUY|SELL)$",
                message = "side must be BUY or SELL"
        )
        String side,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be positive")
        BigDecimal quantity,

        @NotNull(message = "price is required")
        @PositiveOrZero(message = "price must be zero or positive")
        BigDecimal price,

        @NotNull(message = "tradeDate is required")
        LocalDate tradeDate
) {}
