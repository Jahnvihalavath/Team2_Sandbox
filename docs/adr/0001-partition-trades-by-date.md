# ADR-001 - Partition the "trades" table by 'trade_date'

# ADR-0001 — Partition the trades table by trade_date

## Status

Accepted

Date: 2026-06-02

## Context

ReconX processes approximately 50,000 trades per day with a five-year retention
requirement. The trades table will grow to approximately 91 million rows.
Most reconciliation queries filter trades using trade_date ranges.

An unpartitioned table makes date-based queries, archival, and maintenance
operations expensive.

Alternatives considered:
- Single large trades table
- Partitioning by trade_id
- Partitioning by trade_date

## Decision

We will partition the trades table using PostgreSQL RANGE partitioning on
trade_date.

Partitions will be created monthly because analysts commonly query monthly
and daily reconciliation windows.

## Consequences

Positive:
- Faster date-range queries through partition pruning.
- Easier archival by detaching old partitions.
- Smaller indexes per partition.

Negative:
- Primary keys become more complex.
- Partition maintenance requires automation.
- Cross-partition unique constraints need additional handling.