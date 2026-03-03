package com.llmanalytics.domain.repository;

import com.llmanalytics.domain.model.ModelPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModelPricingRepository extends JpaRepository<ModelPricing, UUID> {

    Optional<ModelPricing> findByModelIdentifier(String modelIdentifier);
}
