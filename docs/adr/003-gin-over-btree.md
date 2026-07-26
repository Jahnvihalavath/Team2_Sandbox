# ADR-0003 — Use GIN index for JSONB metadata queries

## Status

Accepted

Date: 2026-06-02

## Context

ReconX analysts need to search instrument metadata stored in JSONB fields.
Queries frequently filter using JSON attributes.

Alternatives considered:
- B-tree indexes
- No index
- PostgreSQL GIN jsonb_path_ops index

## Decision

We will use a PostgreSQL GIN index with jsonb_path_ops on metadata columns.

GIN is optimized for containment queries on JSONB documents.

## Consequences

Positive:
- Faster JSONB lookup operations.
- Supports scalable metadata searches.
- Better performance for analyst queries.

Negative:
- Additional storage usage.
- Index maintenance overhead during writes.
- Not suitable for every query pattern.