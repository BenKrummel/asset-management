package com.exec.asset.management.service.message;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import com.exec.asset.management.domain.messages.AssetPromotionEventModel;
import com.exec.asset.management.event.config.BaseKafkaConfiguration;
import com.exec.asset.management.event.publisher.KafkaMessagingSystemService;
import com.exec.asset.management.util.MultiTenantIdentifierResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AssetPublisherServiceTest {
    private KafkaTemplate kafkaTemplate;
    private AssetPublisherService assetPublisherService;

    @BeforeEach
    void init() {
        kafkaTemplate = mock(KafkaTemplate.class);

        BaseKafkaConfiguration kafkaConfiguration = mock(BaseKafkaConfiguration.class);
        when(kafkaConfiguration.kafkaTemplate()).thenReturn(kafkaTemplate);

        KafkaMessagingSystemService kafkaMessagingSystemService = new KafkaMessagingSystemService(kafkaConfiguration, new MultiTenantIdentifierResolver());
        assetPublisherService = new AssetPublisherService(kafkaMessagingSystemService);
    }

    @Test
    public void publishAssetPromotedEventValidEvent() {
        UUID parentId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        ArgumentCaptor<Message<String>> argumentCaptor = ArgumentCaptor.forClass(Message.class);

        assetPublisherService.publishAssetPromotedEvent(AssetPromotionEventModel.builder()
                .parentId(parentId)
                .assetId(assetId)
                .promoted(true)
                .build());

        verify(kafkaTemplate).send(argumentCaptor.capture());
        Message<String> capturedArgument = argumentCaptor.getValue();

        assertTrue(capturedArgument.getHeaders().containsKey("assetId"));
        assertEquals(assetId.toString(), capturedArgument.getHeaders().get("assetId"));
        assertTrue(capturedArgument.getHeaders().containsKey("parentAssetId"));
        assertEquals(parentId.toString(), capturedArgument.getHeaders().get("parentAssetId"));
        assertTrue(capturedArgument.getPayload().contains(parentId.toString()));
        assertTrue(capturedArgument.getPayload().contains(assetId.toString()));
    }
}
