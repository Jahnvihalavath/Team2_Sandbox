package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.TradeType;
import com.dbtraining.reconx.model.BondTrade;
import com.dbtraining.reconx.model.FXTrade;
import com.dbtraining.reconx.model.DerivativeTrade;
import org.springframework.stereotype.Service;
import com.dbtraining.reconx.model.Side;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ============================================================================
 * TICKET-ADV034 — Trade analytics with Collectors (groupingBy + summarizing)
 * TICKET-ADV035 — VWAP calculator using Streams + custom collector
 * TICKET-ADV036 — P&L per instrument: stream reduction
 * ============================================================================
 */
@Service
public class TradeAnalyticsService {

    /** TICKET-ADV034 — count + sum of notional per counterparty. */
    public Map<Long, NotionalSummary> notionalByCounterparty(List<? extends TradeType> trades) {
        // TODO(TICKET-ADV034): 
        return trades.stream()
            .collect(Collectors.groupingBy(
            this::counterpartyIdOf,
            Collectors.collectingAndThen(
                Collectors.toList(), 
                list->new NationalSummary(
                    list.size(),
                    list.stream()
                        .map(t->t.national().amount())
                        .reduce(BigDecimal.ZERO,BigDecimal::add);
                    )
                )
        
        ));

    }

    /**
     * TICKET-ADV035 — 
     * VWAP = SUM(price * qty) / SUM(qty). Equity-only — only
     */
    public Map<String, BigDecimal> vwapByInstrument(List<EquityTrade> equityTrades) {
        return equityTrades.stream()
            .collect(Collectors.groupingBy(
                EquityTrade::instrumentSymbol,
                // Collectors.collectingAndThen(
                //     Collectors.toList(),
                //     list->{
                //         BigDecimal  totalValue=list.stream()
                //             .map(t->t.price().multiply(t.quantity()))
                //             .reduce(BigDecimal.ZERO,BigDecimal::add);
                //         BigDecimal totalQuantity=list.stream()
                //             .map(EquityTrade::quantity)
                //             .reduce(BigDecimal.ZERO,BigDecimal::add);
                //         return totalValue.divide(
                //             totalQuantity,
                //             8,
                //             RoundingMode.HALF_UP
                //         );
                //     }
                // )
                new VwapCollector;
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list->{
                        BigDecimal  totalValue=list.stream()
                            .map(t->t.price().multiply(t.quantity()))
                            .reduce(BigDecimal.ZERO,BigDecimal::add);
                        BigDecimal totalQuantity=list.stream()
                            .map(EquityTrade::quantity)
                            .reduce(BigDecimal.ZERO,BigDecimal::add);
                        return totalValue.divide(
                            totalQuantity,
                            8,
                            RoundingMode.HALF_UP
                        );
                    }
                )
            ));
    }

    /** TICKET-ADV036 — P&L per instrument symbol (sign by Side). */
    public Map<String, BigDecimal> pnlByInstrument(List<EquityTrade> equityTrades) {

        return equityTrades.stream()
            .collect(Collectors.groupingBy(
                EquityTrade::instrumentSymbol,
                Collectors.mapping(
                        this::pnl,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                )
        ));
    }

    private BigDecimal pnl(EquityTrade t) {
        // TODO(TICKET-ADV036): 
        //BigDecimal abs = price * qty; SELL -> abs, BUY -> abs.negate().
        BigDecimal abs = t.price().multiply(t.quantity());

        return t.side() == Side.SELL
            ? abs
            : abs.negate();
    }

    private long counterpartyIdOf(TradeType t) {
        // TODO(TICKET-ADV018): exhaustive switch over the sealed TradeType
        //   hierarchy returning t.counterpartyId() for each concrete subtype.
        //throw new UnsupportedOperationException("TICKET-ADV018");
        return switch(t){
            case EquityTrade trade->trade.counterpartyId();
            case FXTrade trade->trade.counterpartyId();
            case BondTrade trade->trade.counterpartyId();
            case DerivativeTrade trade->trade.counterpartyId();
        }
    }
    private static class VwapCollector implements Collectors<EquityTrade, VwapCollector.Accumulator,BigDecimal>{
        static class Accumulator{
            BigDecimal totalValue=BigDecimal.ZERO;
            BigDecimal totalQty=BigDecimal.ZERO;
        }
        @Override
        public Supplier<Accumulator> supplier(){
            return Accumulator::new;
        }
        @Override
        public BiConsumer<Accumulator,EquityTrade> accumulator(){
            return(acc,trade)->{
                acc.totalValue=acc.totalValue.add(
                    trade.price(),multiply(trade.quantity())
                );
                acc.totalQty=acc.totalQty.add(
                    trade.quantity()
                );
            };
        }
        @Override
        public Function<Accumulator,BigDecimal> finisher(){
            return acc ->{
                if(acc.totalQty.compareTo(BigDecimal.ZERO)==0){
                    return BigDecimal.ZERO;
                }
                return acc.totalValue.divide(
                    acc.totalQty,
                    4,
                    RoundingMode.HALF_UP
                );
            };
        }
        @Override
        public Set<Characteristics> characteristics(){
            return Collections.emptySet();
        }
    }

    public record NotionalSummary(long count, BigDecimal total) {

    }
}
