package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
import com.dbtraining.reconx.repository.ReconResultRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReconciliationServiceTest {

    @Test
    void testReconcile_savesResultWithMatchedStatus() {

        // Given
        ReconResultRepository repo = mock(ReconResultRepository.class);
        ReconciliationEngine engine = new ReconciliationEngine();
        ReconciliationService service =
                new ReconciliationService(engine, repo);

        EquityTrade internal = equity("ABC-20260603-0001", "10", "100");

EquityTrade external = equity("ABC-20260603-0001", "10", "100");

        // When
        service.runRecon(
                List.of(internal),
                List.of(external),
                ReconciliationRule.EXACT
        );

        // Then
        ArgumentCaptor<ReconResult> captor =
                ArgumentCaptor.forClass(ReconResult.class);

        verify(repo).save(captor.capture());

        assertThat(captor.getValue().tradeRef())
        .isEqualTo("ABC-20260603-0001");

        assertThat(captor.getValue().status())
                .isEqualTo(ReconResult.Status.MATCHED);
    }

    private static EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}