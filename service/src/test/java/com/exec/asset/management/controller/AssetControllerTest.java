package com.exec.asset.management.controller;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;

import com.exec.asset.management.api.model.AssetModel;
import com.exec.asset.management.domain.entities.AssetEntity;
import com.exec.asset.management.domain.messages.AssetPromotionEventModel;
import com.exec.asset.management.exception.AssetDoesNotExistException;
import com.exec.asset.management.exception.MismatchedIds;
import com.exec.asset.management.exception.ParentAssetDoesNotExistException;
import com.exec.asset.management.mapper.AssetMapper;
import com.exec.asset.management.repository.AssetRepository;
import com.exec.asset.management.service.controller.AssetControllerService;
import com.exec.asset.management.service.controller.AssetControllerServiceTest;
import com.exec.asset.management.service.message.AssetPublisherService;
import com.exec.asset.management.service.repository.AssetRepositoryService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DataJpaTest
public class AssetControllerTest {
    @Autowired
    private AssetRepository assetRepository;
    private AssetPublisherService assetPublisherService;
    private AssetController assetController;
    private final AssetMapper assetMapper = new AssetMapper();

    @BeforeEach
    void init() {
        assetPublisherService = mock(AssetPublisherService.class);
        AssetControllerService assetControllerService = new AssetControllerService(new AssetRepositoryService(assetRepository), new AssetMapper(), assetPublisherService);
        assetController = new AssetController(assetControllerService);
    }

    @Test
    public void testGetAssetsByIdWithValidId() {
        UUID assetId = assetRepository.save(AssetEntity.builder().promoted(false).build()).getId();

        var response = assetController.getAssetById(assetId);

        assertFalse(response.getBody().getPromoted());
        assertNull(response.getBody().getParentId());
    }

    @Test
    public void testGetAssetsByIdWithInvalidId() {
        assetRepository.save(AssetEntity.builder().promoted(false).build()).getId();
        Exception resultException = null;

        try {
            assetController.getAssetById(UUID.randomUUID());
        }
        catch (AssetDoesNotExistException e) {
            resultException = e;
        }

        assertNotNull(resultException);
    }

    @Test
    public void listAssets() {
        UUID id = assetRepository.save(AssetEntity.builder().build()).getId();
        assetRepository.save(AssetEntity.builder().build());
        assetRepository.save(AssetEntity.builder().build());

        var response = assetController.listAssets(1, 1);

        assertEquals(1, response.getBody().getMeta().getPage().getPageNumber());
        assertEquals(1, response.getBody().getMeta().getPage().getPageSize());
        assertEquals(3, response.getBody().getMeta().getPage().getTotalCount());
        assertEquals(1, response.getBody().getCount());

        response = assetController.listAssets(0, null);
        assertEquals(AssetController.PAGE_SIZE, response.getBody().getMeta().getPage().getPageSize());
        assertTrue(response.getBody().getEntities().stream().anyMatch(responseModel -> responseModel.getId().equals(id)));
    }

    @Test
    public void testCreateAssetNoParentAsset() {
        AssetModel assetModel = new AssetModel();
        assetModel.setPromoted(false);
        var response = assetController.createAsset(assetModel);
        AssetEntity assetEntity = assetRepository.getById(response.getBody().getId());

        assertFalse(response.getBody().getPromoted());
        assertNull(response.getBody().getParentId());
        assertFalse(assetEntity.getPromoted());
        assertNull(assetEntity.getParentId());
    }

    @Test
    public void testCreateAssetWithValidParentAsset() {
        UUID parentId = assetRepository.save(AssetEntity.builder().build()).getId();

        AssetModel assetModel = new AssetModel();
        assetModel.setPromoted(false);
        assetModel.setParentId(parentId);

        var response = assetController.createAsset(assetModel);
        AssetEntity assetEntity = assetRepository.getById(response.getBody().getId());

        assertFalse(response.getBody().getPromoted());
        assertEquals(parentId, response.getBody().getParentId());
        assertFalse(assetEntity.getPromoted());
        assertEquals(parentId, assetEntity.getParentId());
    }

    @Test
    public void testCreateAssetWithInvalidParentAsset() {
        assetRepository.save(AssetEntity.builder().build());
        AssetModel assetModel = new AssetModel();
        assetModel.setPromoted(false);
        assetModel.setParentId(UUID.randomUUID());

        Exception resultException = null;

        try {
            assetController.createAsset(assetModel);
        }
        catch (ParentAssetDoesNotExistException e) {
            resultException = e;
        }

        assertNotNull(resultException);
    }

    @Test
    public void deleteAssetWithValidId() {
        UUID assetId = assetRepository.save(AssetEntity.builder().build()).getId();

        var response = assetController.deleteAsset(assetId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertTrue(assetRepository.findAll().isEmpty());
    }

    @Test
    public void deleteAssetWithValidIdAndChildrenAssets() {
        UUID randomParentId = UUID.randomUUID();
        UUID assetId = assetRepository.save(AssetEntity.builder().parentId(randomParentId).build()).getId();

        AssetEntity asset2 = assetRepository.save(AssetEntity.builder().parentId(assetId).build());
        AssetEntity asset3 = assetRepository.save(AssetEntity.builder().parentId(assetId).build());

        var response = assetController.deleteAsset(assetId);

        AssetEntity updatedAsset2 = assetRepository.getById(asset2.getId());
        AssetEntity updatedAsset3 = assetRepository.getById(asset3.getId());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(2, assetRepository.findAll().size());
        assertEquals(randomParentId, updatedAsset2.getParentId());
        assertEquals(randomParentId, updatedAsset3.getParentId());
    }

    @Test
    public void deleteAssetInvalidId() {
        assetRepository.save(AssetEntity.builder().build());
        Exception resultException = null;

        try {
            assetController.deleteAsset(UUID.randomUUID());
        }
        catch (AssetDoesNotExistException e) {
            resultException = e;
        }

        assertNotNull(resultException);
        assertFalse(assetRepository.findAll().isEmpty());
    }

    @Test
    public void updateAssetMismatchIdAndAssetModelId() {
        Exception resultException = null;
        AssetModel assetModel = assetMapper.mapAssetEntityToAssetModel(assetRepository.save(AssetEntity.builder().promoted(false).build()));
        assetModel.setPromoted(true);

        try {
            assetController.updateAsset(UUID.randomUUID(), assetModel);
        }
        catch (MismatchedIds e) {
            resultException = e;
        }

        assertNotNull(resultException);
        assertFalse(assetRepository.getById(assetModel.getId()).getPromoted());
    }

    @Test
    public void updateAssetNoChildren() {
        AssetModel assetModel = assetMapper.mapAssetEntityToAssetModel(assetRepository.save(AssetEntity.builder().promoted(false).build()));
        assetModel.setPromoted(true);

        var response = assetController.updateAsset(assetModel.getId(), assetModel);

        verify(assetPublisherService).publishAssetPromotedEvent(any(AssetPromotionEventModel.class));
        assertTrue(response.getBody().getPromoted());
        assertTrue(assetRepository.getById(assetModel.getId()).getPromoted());
    }
}
