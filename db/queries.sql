-- ============================================================================
-- TICKET-ADV010 — VWAP per instrument per day (window function)
-- ============================================================================
SELECT DISTINCT
    t.instrument_id,
    t.trade_date,
    SUM(t.price * t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date)
        / NULLIF(SUM(t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date), 0)
            AS vwap
FROM trades t
WHERE t.deleted_at IS NULL
  AND t.asset_class = 'EQUITY'
ORDER BY t.trade_date DESC, t.instrument_id;


-- ============================================================================
-- TICKET-ADV011 — Recursive CTE: trade lifecycle (execution -> settlement
--                -> recon_break -> resolution)
-- ============================================================================
-- ============================================================================
-- TICKET-ADV011 — Recursive CTE: trade lifecycle rollup
-- ============================================================================

WITH RECURSIVE trade_lifecycle AS (

    -- Base case: every trade starts in EXECUTED stage
    SELECT
        t.id AS trade_id,
        t.trade_ref,
        1 AS stage,
        'EXECUTED' AS stage_name,
        t.created_at AS event_at,
        NULL::text AS event_status
    FROM trades t
    WHERE t.deleted_at IS NULL

    UNION ALL

    -- Recursive step: advance through lifecycle stages
    SELECT
        tl.trade_id,
        tl.trade_ref,
        tl.stage + 1,
        CASE tl.stage
            WHEN 1 THEN 'CONFIRMED'
            WHEN 2 THEN 'SETTLED'
            WHEN 3 THEN 'RECONCILED'
            WHEN 4 THEN 'RESOLVED'
        END AS stage_name,
        s.settlement_date::timestamp AS event_at,
        s.status AS event_status
    FROM trade_lifecycle tl
    JOIN settlements s
        ON s.trade_id = tl.trade_id
    WHERE tl.stage < 5
)

SELECT
    trade_id,
    trade_ref,
    stage,
    stage_name,
    event_at,
    event_status
FROM trade_lifecycle
ORDER BY trade_id, stage;


-- ============================================================================
-- ADV008 — REFRESH the daily-summary materialised view (concurrent so it can
--         run while the dashboard is reading it)
-- ============================================================================
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_recon_summary;


-- ============================================================================
-- ADV009 — JSONB lookup: which instruments have sector = 'Banking'?
-- ============================================================================
SELECT id, symbol, metadata
FROM instruments
WHERE metadata @> '{"sector":"Banking"}'::jsonb;
