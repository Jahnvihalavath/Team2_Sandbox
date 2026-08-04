package com.dbtraining.reconx.repository.entity;

import com.dbtraining.reconx.model.TradeType.AssetClass;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * TICKET-ADV051 — JPA entity Instrument. JSONB metadata column wired via
 * the Hypersistence Utils JsonBinaryType on Postgres; H2 stores it as a
 * plain CLOB via the dialect translation (acceptable for dev).
 */
@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_class", nullable = false, length = 20)
    private AssetClass assetClass;

    @Column(nullable = false, length = 3)
    private String currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private Map<String, Object> metadata = new HashMap<>();

    public Instrument() {}

    public Long getId()         { return id; }
    public String getSymbol()   { return symbol; }
    public String getName()     { return name; }
    public AssetClass getAssetClass(){ return assetClass; }
    public String getCurrency() { return currency; }
    public Map<String, Object> getMetadata() { return metadata; }
}