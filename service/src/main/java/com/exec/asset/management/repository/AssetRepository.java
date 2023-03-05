package com.exec.asset.management.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.exec.asset.management.domain.entities.AssetEntity;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, UUID> {
}
