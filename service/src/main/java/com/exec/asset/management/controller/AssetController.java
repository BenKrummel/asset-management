package com.exec.asset.management.controller;

import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exec.asset.management.api.AssetsApi;
import com.exec.asset.management.api.model.AssetListModel;
import com.exec.asset.management.api.model.AssetModel;
import com.exec.asset.management.api.model.PagedAssetsModel;
import com.exec.asset.management.service.controller.AssetControllerService;

@RestController
@RequestMapping("/v1")
public class AssetController implements AssetsApi {

    protected static final int PAGE_SIZE = 50;

    private AssetControllerService assetControllerService;

    @Autowired
    public AssetController(AssetControllerService assetControllerService) {
        this.assetControllerService = assetControllerService;
    }

    @Override
    public ResponseEntity<AssetListModel> createAsset(AssetListModel assetModel) {
        return ResponseEntity.ok(assetControllerService.createAssetFromList(assetModel));
    }

    @Override
    public ResponseEntity<Void> deleteAsset(UUID id) {
        assetControllerService.deleteAssetById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AssetModel> getAssetById(UUID id) {
        return ResponseEntity.ok(assetControllerService.getAssetById(id));
    }

    @Override
    public ResponseEntity<PagedAssetsModel> listAssets(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(Objects.requireNonNullElse(pageNumber, 0), Objects.requireNonNullElse(pageSize, PAGE_SIZE));
        return ResponseEntity.ok(assetControllerService.getPagedAssets(pageRequest));
    }

    @Override
    public ResponseEntity<AssetModel> updateAsset(UUID id, AssetModel assetModel) {
        return ResponseEntity.ok(assetControllerService.updateAsset(assetModel, id));
    }
}
