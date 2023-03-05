package com.exec.asset.management.domain.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetPromotionEventModel {
    public static final String TOPIC_NAME = "asset.events.asset-promoted";
    public static final String AGGREGATE_NAME = "asset-promotion";
    public static final String CREATED_EVENT_NAME = "asset-promoted";

    private UUID assetId;
    private boolean promoted;
    private UUID parentId;
}
