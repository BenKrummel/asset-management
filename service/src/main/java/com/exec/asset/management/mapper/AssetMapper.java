package com.exec.asset.management.mapper;

import org.springframework.stereotype.Component;

import com.exec.asset.management.api.model.AssetModel;
import com.exec.asset.management.domain.entities.AssetEntity;

@Component
public class AssetMapper {

    public AssetModel mapAssetEntityToAssetModel(AssetEntity assetEntity) {
        AssetModel assetModel = new AssetModel();
        assetModel.setId(assetEntity.getId());
        assetModel.promoted(assetEntity.getPromoted());
        assetModel.setParentId(assetEntity.getParentEntity() != null ? assetEntity.getParentEntity().getId() : null);
        return assetModel;
    }

    public AssetEntity mapAssetModelToAssetEntity(AssetModel assetModel) {
        return AssetEntity.builder()
                .id(assetModel.getId())
                .promoted(assetModel.getPromoted())
                .build();
    }
}
