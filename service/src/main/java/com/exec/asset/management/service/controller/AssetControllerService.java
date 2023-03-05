package com.exec.asset.management.service.controller;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.exec.asset.management.api.model.AssetModel;
import com.exec.asset.management.api.model.MetaModel;
import com.exec.asset.management.api.model.PageMetaModel;
import com.exec.asset.management.api.model.PagedAssetsModel;
import com.exec.asset.management.domain.entities.AssetEntity;
import com.exec.asset.management.exception.AssetDoesNotExistException;
import com.exec.asset.management.exception.MismatchedIds;
import com.exec.asset.management.exception.ParentAssetDoesNotExistException;
import com.exec.asset.management.mapper.AssetMapper;
import com.exec.asset.management.service.repository.AssetRepositoryService;

@Service
@Transactional
@Slf4j
public class AssetControllerService {

    @Autowired
    private AssetRepositoryService assetRepositoryService;
    @Autowired
    private AssetMapper assetMapper;

    public PagedAssetsModel getPagedAssets(PageRequest pageRequest) {
        return pagedAssetsResponse(assetRepositoryService.getAllAssets(pageRequest));
    }

    public AssetModel getAssetById(UUID assetId) {
        return assetMapper.mapAssetEntityToAssetModel(assetRepositoryService.findAssetById(assetId).orElseThrow(() -> new AssetDoesNotExistException(assetId)));
    }

    public void deleteAssetById(UUID assetId) {
        assetRepositoryService.deleteAsset(assetId);
    }

    public AssetModel createAsset(AssetModel assetModel) {
        UUID parentAssetId = assetModel.getId();
        log.debug("AssetControllerService:createAsset: Creating asset with parent id {}", parentAssetId);
        AssetEntity assetEntity = assetMapper.mapAssetModelToAssetEntity(assetModel);

        assetEntity = setParentEntity(assetEntity, parentAssetId);

        return assetMapper.mapAssetEntityToAssetModel(assetRepositoryService.saveAsset(assetEntity));
    }

    public AssetModel updateAsset(AssetModel assetModel, UUID assetId) {
        if (!assetId.equals(assetModel)) {
            throw new MismatchedIds(assetModel.getId(), assetId);
        }

        AssetEntity assetEntity = assetRepositoryService.findAssetById(assetId).orElseThrow(() -> new AssetDoesNotExistException(assetId));

        // If it was already promoted don't promote the children again.
        if (!assetEntity.getPromoted() && assetModel.getPromoted()) {
            promoteChildAssets(assetEntity);
        }

        assetEntity = setParentEntity(assetEntity, assetModel.getParentId());
        assetEntity.setPromoted(assetModel.getPromoted());

        return assetMapper.mapAssetEntityToAssetModel(assetRepositoryService.saveAsset(assetEntity));
    }

    private AssetEntity setParentEntity(AssetEntity assetEntity, UUID parentAssetId) {
        if (parentAssetId != null) {
            log.debug("AssetControllerService:createAsset: Finding parent asset with id {}", parentAssetId);
            assetEntity.setParentEntity(assetRepositoryService.findAssetById(parentAssetId).orElseThrow(() -> new ParentAssetDoesNotExistException(parentAssetId)));
        }
        return assetEntity;
    }

    private void promoteChildAssets(AssetEntity assetEntity) {

    }

    private PagedAssetsModel pagedAssetsResponse(Page<AssetEntity> assetEntities) {
        Pageable pageable = assetEntities.getPageable();
        PageMetaModel pageMeta = new PageMetaModel();
        pageMeta.setPageNumber(pageable.getPageNumber());
        pageMeta.setPageSize(pageable.getPageSize());
        pageMeta.setTotalCount(assetEntities.getTotalElements());

        return new PagedAssetsModel().meta(new MetaModel().page(pageMeta))
                .count(assetEntities.getNumberOfElements())
                .entities(assetEntities.stream().map(assetEntity -> assetMapper.mapAssetEntityToAssetModel(assetEntity))
                        .collect(Collectors.toList()));
    }


}
