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

import com.exec.asset.management.api.model.AssetListModel;
import com.exec.asset.management.api.model.AssetModel;
import com.exec.asset.management.api.model.MetaModel;
import com.exec.asset.management.api.model.PageMetaModel;
import com.exec.asset.management.api.model.PagedAssetsModel;
import com.exec.asset.management.domain.entities.AssetEntity;
import com.exec.asset.management.exception.AssetDoesNotExistException;
import com.exec.asset.management.exception.AssetIdCannotBeNullException;
import com.exec.asset.management.exception.MismatchedIds;
import com.exec.asset.management.exception.ParentAssetDoesNotExistException;
import com.exec.asset.management.exception.ParentAssetRequiredException;
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

    public AssetListModel createAssetFromList(AssetListModel assetCreationModel) {
        UUID parentId = null;
        AssetListModel returnModels = new AssetListModel();

        if (assetCreationModel.getParentAsset() != null) {
            log.debug("AssetControllerService:createAssetFromList: Creating parent Asset");
            AssetModel parentModel = createAsset(assetCreationModel.getParentAsset());
            returnModels.setParentAsset(parentModel);
            parentId = parentModel.getId();
        }

        if (assetCreationModel.getChildAssets() != null) {
            UUID finalParentId = parentId;
            assetCreationModel.getChildAssets().stream().forEach(assetModel -> {
                if (assetModel.getId() == null) {
                    log.debug("AssetControllerService:createAssetFromList: No id on child asset so we don't need to check if it exists.");
                    assetModel.setParentId(finalParentId);
                    returnModels.addChildAssetsItem(createAsset(assetModel));
                }
                else {
                    // We are setting the parent id to the id of the parent model we just created.
                    assetRepositoryService.findAssetById(assetModel.getId()).ifPresentOrElse(assetEntity -> {
                        log.debug("AssetControllerService:createAssetFromList: Found child asset so just updating parent id to: {}", finalParentId);
                        assetEntity.setParentId(finalParentId);
                        assetRepositoryService.saveAsset(assetEntity);
                        returnModels.addChildAssetsItem(assetMapper.mapAssetEntityToAssetModel(assetEntity));
                    }, () -> {
                        log.debug("AssetControllerService:createAssetFromList: No child asset found for id so creating new entity");
                        assetModel.setParentId(finalParentId);
                        returnModels.addChildAssetsItem(createAsset(assetModel));
                    });
                }
            });
        }

        return returnModels;
    }

    public AssetModel updateAssetList(AssetListModel assetListModel, UUID assetId) {
        AssetModel parentModel = assetListModel.getParentAsset();
        if (parentModel == null) {
            throw new ParentAssetRequiredException();
        }
        if (!assetId.equals(parentModel.getId())) {
            throw new MismatchedIds(parentModel.getId(), assetId);
        }

        log.debug("AssetControllerService:updateAssetList: Updating asset with id: {}", assetId);
        AssetEntity assetEntity = assetRepositoryService.findAssetById(assetId).orElseThrow(() -> new AssetDoesNotExistException(assetId));

        if (assetListModel.getChildAssets() != null) {
            log.debug("AssetControllerService:updateAssetList: Updating child entities passed in to have the parent id updated.");
            assetListModel.getChildAssets().forEach(assetModel -> {
                if (assetModel.getId() == null) {
                    throw new AssetIdCannotBeNullException();
                }
                AssetEntity updateEntity = assetRepositoryService.findAssetById(assetModel.getId()).orElseThrow(() -> new AssetDoesNotExistException(assetModel.getId()));
                updateEntity.setParentId(assetId);
                assetRepositoryService.saveAsset(updateEntity);
            });
        }

        // This is under the assumption if we modify an existing asset we shouldn't call promote children if the entity was previously promoted.
        // If this assumption is wrong then the if statement would change to assetModel.getPromoted()
        if (!assetEntity.getPromoted() && parentModel.getPromoted()) {
            log.debug("AssetControllerService:updateAssetList: Promoting the asset and its child assets.");
            promoteChildAssets(assetEntity);
        }

        assetEntity = setParentIdIfValid(assetEntity, parentModel.getParentId());
        assetEntity.setPromoted(parentModel.getPromoted());
        return assetMapper.mapAssetEntityToAssetModel(assetRepositoryService.saveAsset(assetEntity));
    }

    private AssetModel createAsset(AssetModel assetModel) {
        UUID parentAssetId = assetModel.getParentId();
        log.debug("AssetControllerService:createAsset: Creating asset with parent id {}", parentAssetId);
        AssetEntity assetEntity = assetMapper.mapAssetModelToAssetEntity(assetModel);
        assetEntity = setParentIdIfValid(assetEntity, parentAssetId);
        return assetMapper.mapAssetEntityToAssetModel(assetRepositoryService.saveAsset(assetEntity));
    }

    private AssetEntity setParentIdIfValid(AssetEntity assetEntity, UUID parentAssetId) {
        if (parentAssetId != null) {
            log.debug("AssetControllerService:setParentIdIfValid: Finding parent asset with id {}", parentAssetId);
            AssetEntity parentAssetEntity = assetRepositoryService.findAssetById(parentAssetId).orElseThrow(() -> new ParentAssetDoesNotExistException(parentAssetId));
            assetEntity.setParentId(parentAssetEntity.getId());
        }
        return assetEntity;
    }

    private void promoteChildAssets(AssetEntity assetEntity) {
        log.debug("AssetControllerService:promoteChildAssets: Promoting child assets and asset with id {}", assetEntity.getId());
        // Update the asset and its nested assets
        List<UUID> usedUuids = new ArrayList<>();
        promoteAssetAndNestedAssets(assetEntity, usedUuids);
    }

    private void promoteAssetAndNestedAssets(AssetEntity asset, List<UUID> usedUuids) {
        log.debug("AssetControllerService:promoteAssetAndNestedAssets: Attempting to promote asset id {} previously promoted: {}", asset.getId(), asset.getPromoted());
        /*
         * If an asset was already promoted previously we don't need to promote it again.
         * We still have to search its children though since they could have been added
         * after the asset was promoted.
         */
        if (!asset.getPromoted()) {
            log.debug("AssetControllerService:promoteAssetAndNestedAssets: Promoting asset with id {}", asset.getId());
            asset.setPromoted(true);
            assetRepositoryService.saveAsset(asset);
            // Publish message to the asset.events.asset-promoted topic.
            assetPublisherService.publishAssetPromotedEvent(assetMapper.mapAssetEntityToAssetPromotionEventModel(asset));
        }

        // Lets make sure that the id that we are using wasn't already used to find children.
        // So we avoid a circular dependency which would make us loop indefinitely.
        if (usedUuids.contains(asset.getId())) {
            log.debug("AssetControllerService:promoteAssetAndNestedAssets: Found a circling dependency so returning. Asset id: {} Parent id: ", asset.getId(), asset.getParentId());
            return;
        }

        // add the asset id to a list of asset ids that we have previously traversed.
        usedUuids.add(asset.getId());
        List<AssetEntity> nestedAssets = assetRepositoryService.getAssetsByParentId(asset.getId());

        if (nestedAssets.isEmpty()) {
            log.debug("AssetControllerService:promoteAssetAndNestedAssets: No child assets for parentId: {}", asset.getId());
            return;
        }
        log.debug("AssetControllerService:promoteAssetAndNestedAssets: Found child assets number: {} parentId: {}", nestedAssets.size(), asset.getId());
        // Would want to make this parallel at some point but deadlocks occur since a thread would update the child asset while another is trying to use that asset.
        nestedAssets.forEach(nestedAsset -> promoteAssetAndNestedAssets(nestedAsset, usedUuids));

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
