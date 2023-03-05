package com.exec.asset.management.service.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exec.asset.management.domain.messages.AssetAggregateEvent;
import com.exec.asset.management.domain.messages.AssetPromotionEventModel;
import com.exec.asset.management.event.publisher.KafkaMessagingSystemService;

@Service
public class AssetPublisherService {

    private final KafkaMessagingSystemService kafkaMessagingSystemService;

    @Autowired
    public AssetPublisherService(KafkaMessagingSystemService kafkaMessagingSystemService) {
        this.kafkaMessagingSystemService = kafkaMessagingSystemService;
    }

    public void publishAssetPromotedEvent(AssetPromotionEventModel assetPromotionEventModel) {
        kafkaMessagingSystemService.send(
                AssetAggregateEvent.builder()
                        .aggregate(assetPromotionEventModel)
                        .assetId(assetPromotionEventModel.getAssetId().toString())
                        .parentId(String.valueOf(assetPromotionEventModel.getParentId()))
                        .eventName(AssetPromotionEventModel.CREATED_EVENT_NAME)
                        .msgSysTopic(AssetPromotionEventModel.TOPIC_NAME)
                        .build());
    }
}
