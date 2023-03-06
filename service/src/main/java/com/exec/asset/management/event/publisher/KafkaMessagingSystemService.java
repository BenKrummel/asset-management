package com.exec.asset.management.event.publisher;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import com.exec.asset.management.domain.messages.AssetAggregateEvent;
import com.exec.asset.management.domain.messages.AssetAggregateEventEntity;
import com.exec.asset.management.event.config.BaseKafkaConfiguration;
import com.exec.asset.management.util.JsonUtils;
import com.exec.asset.management.util.MultiTenantIdentifierResolver;

@Slf4j
@Component
public class KafkaMessagingSystemService {

    private BaseKafkaConfiguration kafkaProducerConfig;
    @Autowired
    private MultiTenantIdentifierResolver multiTenantIdentifierResolver;

    /**
     * It initialize the KafkaTemplate.
     */
    public KafkaMessagingSystemService(BaseKafkaConfiguration kafkaProducerConfig, MultiTenantIdentifierResolver multiTenantIdentifierResolver) {
        this.kafkaProducerConfig = kafkaProducerConfig;
        this.multiTenantIdentifierResolver = multiTenantIdentifierResolver;
    }

    public ListenableFuture<SendResult<String, String>> send(AssetAggregateEvent aae) {
        AssetAggregateEventEntity aggregateEventEntity = mapToEntity(aae);
        Message<String> message = createMessage(aggregateEventEntity);
        KafkaTemplate<String, String> kafkaTemplate = kafkaProducerConfig.kafkaTemplate();

        log.debug("Sending message {topic={}, eventId={}, assetId={}, parentAssetId= {}}", aggregateEventEntity.getAssetId(),
                aggregateEventEntity.getEventName(), aggregateEventEntity.getEventId(), aggregateEventEntity.getParentId());
        kafkaTemplate.setDefaultTopic(aae.getMsgSysTopic());
        return kafkaTemplate.send(message);
    }

    AssetAggregateEventEntity mapToEntity(AssetAggregateEvent aee) {
        Object o = aee.getAggregate();
        String payload = o.getClass() == String.class ? (String) o : JsonUtils.toJson(o);
        Date createdAt = new Date();

        return AssetAggregateEventEntity.builder()
                .eventId(UUID.randomUUID())
                .applicationInstanceId(aee.getApplicationInstanceId())
                .msgSysHeaders(aee.getMsgSysHeaders())
                .assetId(aee.getAssetId())
                .parentId(aee.getParentId())
                .tenantId(multiTenantIdentifierResolver.resolveCurrentTenantIdentifier())
                .eventName(aee.getEventName())
                .payload(payload)
                .partitionKey(aee.getPartitionKey())
                .createdAt(createdAt)
                .subjectId(aee.getSubjectId())
                .build();
    }

    /**
     * Create the message to be send to messaging service.
     *
     * @param aggregateEventEntity AssetAggregateEventEntity
     * @return Message
     */
    protected Message<String> createMessage(AssetAggregateEventEntity aggregateEventEntity) {
        AssetAggregateEventMessage<String> message = AssetAggregateEventMessage.of(aggregateEventEntity);
        Map<String, Object> stdHeaders = message.getHeaders().toMap();
        MessageBuilder<String> builder = MessageBuilder.withPayload(message.getPayload());

        if (aggregateEventEntity.getPartitionKey() != null) {
            builder.setHeader(KafkaHeaders.MESSAGE_KEY, aggregateEventEntity.getPartitionKey());
        }
        return builder
                .copyHeaders(aggregateEventEntity.getMsgSysHeaders())
                .copyHeaders(stdHeaders)
                .setHeader(KafkaHeaders.TOPIC, aggregateEventEntity.getMsgSysTopic())
                .build();
    }
}