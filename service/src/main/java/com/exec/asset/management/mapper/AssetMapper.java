package com.exec.asset.management.mapper;

import org.springframework.stereotype.Component;

import com.exec.asset.management.api.model.AssetModel;
import com.exec.asset.management.domain.entities.AssetEntity;
import com.exec.asset.management.domain.messages.AssetPromotionEventModel;

@Component
public class AssetMapper {

    public AssetModel mapAssetEntityToAssetModel(AssetEntity assetEntity) {
        AssetModel assetModel = new AssetModel();
        assetModel.setId(assetEntity.getId());
        assetModel.promoted(assetEntity.getPromoted());
        assetModel.setParentId(assetEntity.getParentId());
        return assetModel;
    }

    public AssetEntity mapAssetModelToAssetEntity(AssetModel assetModel) {
        return AssetEntity.builder()
                .id(assetModel.getId())
                .promoted(assetModel.getPromoted())
                .build();
    }

    public AssetPromotionEventModel mapAssetEntityToAssetPromotionEventModel(AssetEntity assetEntity) {
        return AssetPromotionEventModel.builder()
                .assetId(assetEntity.getId())
                .promoted(assetEntity.getPromoted())
                .parentId(assetEntity.getParentId())
                .build();
    }
}
