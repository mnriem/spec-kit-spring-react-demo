package com.llmanalytics.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "model_pricing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "model_identifier", nullable = false, unique = true, length = 100)
    private String modelIdentifier;

    @Column(name = "input_price_per_million_tokens", nullable = false, precision = 12, scale = 6)
    private BigDecimal inputPricePerMillionTokens;

    @Column(name = "output_price_per_million_tokens", nullable = false, precision = 12, scale = 6)
    private BigDecimal outputPricePerMillionTokens;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
