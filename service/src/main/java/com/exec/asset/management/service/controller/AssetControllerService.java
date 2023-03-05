package com.exec.asset.management.service.controller;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
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
import com.exec.asset.management.event.publisher.KafkaMessagingSystemService;
import com.exec.asset.management.exception.AssetDoesNotExistException;
import com.exec.asset.management.exception.MismatchedIds;
import com.exec.asset.management.exception.ParentAssetDoesNotExistException;
import com.exec.asset.management.mapper.AssetMapper;
import com.exec.asset.management.service.message.AssetPublisherService;
import com.exec.asset.management.service.repository.AssetRepositoryService;

@Service
@Transactional
@Slf4j
public class AssetControllerService {

    private AssetRepositoryService assetRepositoryService;
    private AssetMapper assetMapper;
    private AssetPublisherService assetPublisherService;
    private final List<AssetEntity> updatedAssetList = new ArrayList<>();

    @Autowired
    public AssetControllerService(AssetRepositoryService assetRepositoryService, AssetMapper assetMapper, AssetPublisherService assetPublisherService) {
        this.assetRepositoryService = assetRepositoryService;
        this.assetMapper = assetMapper;
        this.assetPublisherService = assetPublisherService;
    }

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
        UUID parentAssetId = assetModel.getParentId();
        log.debug("AssetControllerService:createAsset: Creating asset with parent id {}", parentAssetId);
        AssetEntity assetEntity = assetMapper.mapAssetModelToAssetEntity(assetModel);

        assetEntity = setParentIdIfValid(assetEntity, parentAssetId);

        return assetMapper.mapAssetEntityToAssetModel(assetRepositoryService.saveAsset(assetEntity));
    }

    public AssetModel updateAsset(AssetModel assetModel, UUID assetId) {
        if (!assetId.equals(assetModel)) {
            throw new MismatchedIds(assetModel.getId(), assetId);
        }

        AssetEntity assetEntity = assetRepositoryService.findAssetById(assetId).orElseThrow(() -> new AssetDoesNotExistException(assetId));

        // This is under the assumption if we modify an existing asset we shouldn't call promote children if the entity was previously promoted.
        // If this assumption is wrong then the if statement would change to assetModel.getPromoted()
        if (!assetEntity.getPromoted() && assetModel.getPromoted()) {
            promoteChildAssets(assetEntity);
        }

        assetEntity = setParentIdIfValid(assetEntity, assetModel.getParentId());
        assetEntity.setPromoted(assetModel.getPromoted());

        return assetMapper.mapAssetEntityToAssetModel(assetRepositoryService.saveAsset(assetEntity));
    }

    private AssetEntity setParentIdIfValid(AssetEntity assetEntity, UUID parentAssetId) {
        if (parentAssetId != null) {
            log.debug("AssetControllerService:createAsset: Finding parent asset with id {}", parentAssetId);
            AssetEntity parentAssetEntity = assetRepositoryService.findAssetById(parentAssetId).orElseThrow(() -> new ParentAssetDoesNotExistException(parentAssetId));
            assetEntity.setParentId(parentAssetEntity.getId());
        }
        return assetEntity;
    }

    public void promoteChildAssets(AssetEntity assetEntity) {
        // Update the asset and its nested assets
        promoteAssetAndNestedAssets(assetEntity);

        // Save the updated assets in a single database call
        assetRepositoryService.saveAll(updatedAssetList);
    }

    private void promoteAssetAndNestedAssets(AssetEntity asset) {
        asset.setPromoted(true);
        updatedAssetList.add(asset);
        assetPublisherService.publishAssetPromotedEvent(assetMapper.mapAssetEntityToAssetPromotionEventModel(asset));

        List<AssetEntity> nestedAssets = assetRepositoryService.getAssetsByParentId(asset.getId());
        if (nestedAssets != null) {
            // Process nested assets in parallel
            nestedAssets.parallelStream().forEach(nestedAsset -> promoteAssetAndNestedAssets(nestedAsset));
        }
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
