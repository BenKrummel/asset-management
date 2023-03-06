package com.exec.asset.management.service.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.exec.asset.management.api.model.AssetListModel;
import com.exec.asset.management.api.model.AssetModel;
import com.exec.asset.management.domain.entities.AssetEntity;
import com.exec.asset.management.domain.messages.AssetPromotionEventModel;
import com.exec.asset.management.exception.ParentAssetRequiredException;
import com.exec.asset.management.mapper.AssetMapper;
import com.exec.asset.management.repository.AssetRepository;
import com.exec.asset.management.service.message.AssetPublisherService;
import com.exec.asset.management.service.repository.AssetRepositoryService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    public void updateAssetListErrorNoParentAsset() {
        Exception e = null;

        try {
            assetControllerService.updateAssetList(new AssetListModel(), UUID.randomUUID());
        }
        catch(ParentAssetRequiredException ex) {
            e = ex;
        }

        assertNotNull(e);
    }

    @Test
    public void updateAssetListWithChildren() {
        AssetModel assetModel = assetMapper.mapAssetEntityToAssetModel(assetRepository.save(AssetEntity.builder().promoted(false).build()));
        AssetEntity secondLevelAsset = assetRepository.save(AssetEntity.builder().promoted(false).parentId(assetModel.getId()).build());
        AssetEntity secondLevelAsset2 = assetRepository.save(AssetEntity.builder().promoted(false).build());
        assetRepository.save(AssetEntity.builder().promoted(false).parentId(secondLevelAsset2.getId()).build());
        assetRepository.save(AssetEntity.builder().promoted(false).parentId(secondLevelAsset.getId()).build());
        assetModel.setPromoted(true);

        AssetListModel assetListModel = new AssetListModel();
        assetListModel.setParentAsset(assetModel);
        assetListModel.addChildAssetsItem(assetMapper.mapAssetEntityToAssetModel(secondLevelAsset));
        assetListModel.addChildAssetsItem(assetMapper.mapAssetEntityToAssetModel(secondLevelAsset2));

        var response = assetControllerService.updateAssetList(assetListModel, assetModel.getId());

        verify(assetPublisherService, Mockito.times(5)).publishAssetPromotedEvent(any(AssetPromotionEventModel.class));
        assertTrue(response.getPromoted());
        assertTrue(assetRepository.getById(assetModel.getId()).getPromoted());
        assertTrue(assetRepository.findByParentId(assetModel.getId()).stream().allMatch(entity -> entity.getPromoted()));
        assertTrue(assetRepository.findByParentId(secondLevelAsset.getId()).stream().allMatch(entity -> entity.getPromoted()));
        assertTrue(assetRepository.findByParentId(secondLevelAsset2.getId()).stream().allMatch(entity -> entity.getPromoted()));
        // Make sure we updated the asset to have the parentId.
        assertEquals(assetModel.getId(), assetRepository.getById(secondLevelAsset2.getId()).getParentId());
    }

    @Test
    public void updateAssetListWithCircularDependency() {
        AssetEntity topLevelAsset = assetRepository.save(AssetEntity.builder().promoted(false).build());
        AssetEntity secondLevelAsset = assetRepository.save(AssetEntity.builder().promoted(false).parentId(topLevelAsset.getId()).build());
        assetRepository.save(AssetEntity.builder().promoted(false).parentId(secondLevelAsset.getId()).build());

        topLevelAsset.setParentId(secondLevelAsset.getId());
        AssetModel assetModel = assetMapper.mapAssetEntityToAssetModel(assetRepository.save(topLevelAsset));
        assetModel.setPromoted(true);

        AssetListModel assetListModel = new AssetListModel();
        assetListModel.setParentAsset(assetModel);

        var response = assetControllerService.updateAssetList(assetListModel, assetModel.getId());

        verify(assetPublisherService, Mockito.times(3)).publishAssetPromotedEvent(any(AssetPromotionEventModel.class));
        assertTrue(response.getPromoted());
        assertTrue(assetRepository.getById(assetModel.getId()).getPromoted());
        assertTrue(assetRepository.findByParentId(assetModel.getId()).stream().allMatch(entity -> entity.getPromoted()));
        assertTrue(assetRepository.findByParentId(secondLevelAsset.getId()).stream().allMatch(entity -> entity.getPromoted()));
    }

    @Test
    public void createAssetFromListWithChildrenThatAlreadyExistAndOnesThatDoNot() {
        AssetEntity childAsset = assetRepository.save(AssetEntity.builder().promoted(false).parentId(UUID.randomUUID()).build());

        AssetListModel assetListModel = new AssetListModel();
        assetListModel.setParentAsset(createAssetModel(null));
        assetListModel.addChildAssetsItem(createAssetModel(assetListModel.getParentAsset().getParentId()));
        assetListModel.addChildAssetsItem(assetMapper.mapAssetEntityToAssetModel(childAsset));

        AssetModel assetModelWithId = createAssetModel(assetListModel.getParentAsset().getParentId());
        assetModelWithId.setId(UUID.randomUUID());
        assetListModel.addChildAssetsItem(assetModelWithId);

        var response = assetControllerService.createAssetFromList(assetListModel);
        UUID parentId = response.getParentAsset().getId();

        assertNull(assetRepository.getById(response.getParentAsset().getId()).getParentId());
        assertEquals(parentId, assetRepository.getById(response.getChildAssets().get(0).getId()).getParentId());
        assertEquals(parentId, assetRepository.getById(response.getChildAssets().get(1).getId()).getParentId());
        assertEquals(parentId, assetRepository.getById(response.getChildAssets().get(2).getId()).getParentId());
        assertEquals(parentId, assetRepository.getById(childAsset.getId()).getParentId());
        assertEquals(4, assetRepository.findAll().size());
    }

    private AssetModel createAssetModel(UUID parentId) {
        AssetModel assetModel = new AssetModel();
        assetModel.setPromoted(false);
        assetModel.setParentId(parentId);
        return assetModel;
    }
}
