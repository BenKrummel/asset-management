package com.exec.asset.management.controller;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;

import com.exec.asset.management.api.model.AssetModel;
import com.exec.asset.management.domain.entities.AssetEntity;
import com.exec.asset.management.exception.AssetDoesNotExistException;
import com.exec.asset.management.exception.ParentAssetDoesNotExistException;
import com.exec.asset.management.mapper.AssetMapper;
import com.exec.asset.management.repository.AssetRepository;
import com.exec.asset.management.service.controller.AssetControllerService;
import com.exec.asset.management.service.repository.AssetRepositoryService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class AssetControllerTest {
    @Autowired
    private AssetRepository assetRepository;
    private AssetController assetController;

    @BeforeEach
    void init() {
        AssetControllerService assetControllerService = new AssetControllerService(new AssetRepositoryService(assetRepository), new AssetMapper());
        assetController = new AssetController(assetControllerService);
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
}
