# ADR-0002 — Use JSONB for instrument metadata

## Status

Accepted

Date: 2026-06-02

## Context

ReconX stores trade and instrument information from multiple sources.
Different external systems provide varying metadata fields.

Alternatives considered:
- Separate relational tables for every metadata type
- VARCHAR serialized JSON
- PostgreSQL JSONB

## Decision

We will store flexible instrument metadata using PostgreSQL JSONB.

JSONB provides schema flexibility while supporting indexing and querying.

## Consequences

Positive:
- Supports evolving metadata structures.
- Avoids frequent schema migrations.
- PostgreSQL provides JSON querying capabilities.

Negative:
- Some relational constraints cannot be enforced.
- Queries require careful indexing.
- Data validation must happen in application code.