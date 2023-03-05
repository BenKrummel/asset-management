package com.exec.asset.management.service.repository;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.exec.asset.management.domain.entities.AssetEntity;
import com.exec.asset.management.exception.AssetDoesNotExistException;
import com.exec.asset.management.repository.AssetRepository;

@Service
@Slf4j
public class AssetRepositoryService {
    @Autowired
    private AssetRepository assetRepository;

    public AssetEntity saveAsset(AssetEntity assetEntity) {
        log.debug("AssetRepositoryService:saveAsset: Saving asset id: {}", assetEntity.getId());
        return assetRepository.save(assetEntity);
    }

    public Optional<AssetEntity> findAssetById(UUID id) {
        log.debug("AssetRepositoryService:findAssetById: Finding asset with id: {}", id);
        return assetRepository.findById(id);
    }

    public void deleteAsset(UUID id) {
        log.debug("AssetRepositoryService:deleteAsset: Delete asset with id: {}", id);
        findAssetById(id).orElseThrow(() -> new AssetDoesNotExistException(id));
        assetRepository.deleteById(id);
    }

    public Page<AssetEntity> getAllAssets(PageRequest pageRequest) {
        log.debug("AssetRepositoryService:getAllAddresses: get: {} assets per page", pageRequest.getPageSize());
        return assetRepository.findAll(pageRequest);
    }
}
