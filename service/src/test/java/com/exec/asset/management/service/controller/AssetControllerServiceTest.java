package com.exec.asset.management.service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.exec.asset.management.api.model.AssetModel;
import com.exec.asset.management.domain.entities.AssetEntity;
import com.exec.asset.management.domain.messages.AssetPromotionEventModel;
import com.exec.asset.management.mapper.AssetMapper;
import com.exec.asset.management.repository.AssetRepository;
import com.exec.asset.management.service.message.AssetPublisherService;
import com.exec.asset.management.service.repository.AssetRepositoryService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DataJpaTest
public class AssetControllerServiceTest {
    @Autowired
    private AssetRepository assetRepository;
    private AssetPublisherService assetPublisherService;
    private AssetControllerService assetControllerService;
    private final AssetMapper assetMapper = new AssetMapper();

    @BeforeEach
    void init() {
        assetPublisherService = mock(AssetPublisherService.class);
        assetControllerService = new AssetControllerService(new AssetRepositoryService(assetRepository), new AssetMapper(), assetPublisherService);
    }

    @Test
    public void updateAssetWithChildren() {
        AssetModel assetModel = assetMapper.mapAssetEntityToAssetModel(assetRepository.save(AssetEntity.builder().promoted(false).build()));
        AssetEntity secondLevelAsset = assetRepository.save(AssetEntity.builder().promoted(false).parentId(assetModel.getId()).build());
        AssetEntity secondLevelAsset2 = assetRepository.save(AssetEntity.builder().promoted(false).parentId(assetModel.getId()).build());
        assetRepository.save(AssetEntity.builder().promoted(false).parentId(secondLevelAsset2.getId()).build());
        assetRepository.save(AssetEntity.builder().promoted(false).parentId(secondLevelAsset.getId()).build());
        assetModel.setPromoted(true);

        var response = assetControllerService.updateAsset(assetModel, assetModel.getId());

        verify(assetPublisherService, Mockito.times(5)).publishAssetPromotedEvent(any(AssetPromotionEventModel.class));
        assertTrue(response.getPromoted());
        assertTrue(assetRepository.getById(assetModel.getId()).getPromoted());
        assertTrue(assetRepository.findByParentId(assetModel.getId()).stream().allMatch(entity -> entity.getPromoted()));
        assertTrue(assetRepository.findByParentId(secondLevelAsset.getId()).stream().allMatch(entity -> entity.getPromoted()));
        assertTrue(assetRepository.findByParentId(secondLevelAsset2.getId()).stream().allMatch(entity -> entity.getPromoted()));
    }

    @Test
    public void updateAssetWithCircularDependency() {
        AssetEntity topLevelAsset = assetRepository.save(AssetEntity.builder().promoted(false).build());
        AssetEntity secondLevelAsset = assetRepository.save(AssetEntity.builder().promoted(false).parentId(topLevelAsset.getId()).build());
        assetRepository.save(AssetEntity.builder().promoted(false).parentId(secondLevelAsset.getId()).build());

        topLevelAsset.setParentId(secondLevelAsset.getId());
        AssetModel assetModel = assetMapper.mapAssetEntityToAssetModel(assetRepository.save(topLevelAsset));
        assetModel.setPromoted(true);

        var response = assetControllerService.updateAsset(assetModel, assetModel.getId());

        verify(assetPublisherService, Mockito.times(3)).publishAssetPromotedEvent(any(AssetPromotionEventModel.class));
        assertTrue(response.getPromoted());
        assertTrue(assetRepository.getById(assetModel.getId()).getPromoted());
        assertTrue(assetRepository.findByParentId(assetModel.getId()).stream().allMatch(entity -> entity.getPromoted()));
        assertTrue(assetRepository.findByParentId(secondLevelAsset.getId()).stream().allMatch(entity -> entity.getPromoted()));
    }
}
