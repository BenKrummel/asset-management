package com.exec.asset.management.event.publisher;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.messaging.Message;

import com.exec.asset.management.domain.messages.AssetAggregateEventEntity;
import com.exec.asset.management.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Aggregate event message representation.
 * <p/>
 * Reference: <a href="https://www.asyncapi.com/docs/specifications/2.0.0/#definitionsMessage">
 * Async API Message Object Example</a>
 * <br/>
 * Sample format for the event message: -
 * <p/>
 * <pre>
 * {
 *   "headers": {
 *     "eventName": "asset-promoted",
 *     "eventId": "2b888578-02a8-450e-935f-14d4c34846b4",
 *     "applicationInstanceId": "hello-world-publisher",
 *     "tenantId": "tenant-id-01",
 *     "assetId": "2b888578-02a8-450e-935f-14d4c34846b4",
 *     "parentAssetId": "aa289f8e-bb6d-11ed-afa1-0242ac120002",
 *     "createdAt": "2020-07-07T15:32:21.156+0000",
 *     "subjectId": "subject"
 *     ...
 *   },
 *   "payload": {
 *     "assetId": "2b888578-02a8-450e-935f-14d4c34846b4",
 *     "promoted": true,
 *     "parentAssetId": "aa289f8e-bb6d-11ed-afa1-0242ac120002"
 *   }
 * }
 * </pre>
 */
@Getter
@ToString
public class AssetAggregateEventMessage<T> {

    /**
     * Aggregate/entity event message headers.
     */
    private final Headers headers;

    /**
     * Aggregate/entity aka message payload.
     */
    private final T payload;

    /**
     * {@link Message} used for {@link AssetAggregateEventMessage} creation.
     */
    @JsonIgnore
    private final Message<?> rawMessage;

    /**
     * Constructs {@link AssetAggregateEventMessage} using given headers and payload. This constructor also acts as
     * JsonCreator
     *
     * @param headers message headers.
     * @param payload message paylod.
     */
    @JsonCreator
    public AssetAggregateEventMessage(@JsonProperty("headers") Headers headers, @JsonProperty("payload") T payload) {
        this(headers, payload, null);
    }

    /**
     * Constructs {@link AssetAggregateEventMessage}.
     *
     * @param headers    message headers.
     * @param payload    message paylod.
     * @param rawMessage {@link Message} instance associated with this.
     */
    public AssetAggregateEventMessage(Headers headers, T payload, Message<?> rawMessage) {
        this.headers = headers;
        this.payload = payload;
        this.rawMessage = rawMessage;
    }

    /**
     * Aggregate/entity event message headers representation.
     */
    @Slf4j
    @Data
    public static class Headers {
        public static final String EVENT_NAME_HEADER_NAME = "eventName";
        public static final String AGGREGATE_KEY_HEADER_NAME = "aggregateKey";
        public static final String DP_AGGREGATE_NAME_HEADER_NAME = "entityType";
        public static final String APPLICATION_INSTANCE_ID_HEADER_NAME = "applicationInstanceId";
        public static final String TENANT_ID_HEADER_NAME = "tenantId";
        public static final String CREATED_AT_HEADER_NAME = "createdAt";
        public static final String ASSET_ID_HEADER_NAME = "assetId";
        public static final String PARENT_ASSET_ID_HEADER_NAME = "parentAssetId";
        public static final String EVENT_ID_HEADER_NAME = "eventId";
        public static final String SUBJECT_ID_HEADER_NAME = "subjectId";
        public static final String TENANT_NAME_HEADER_NAME = "tenantName";

        public static final String DEFAULT_EVENT_NAME_VALUE = "no-event-name";
        public static final String DEFAULT_AGGREGATE_KEY_VALUE = "no-aggregate-key";
        public static final String DEFAULT_APPLICATION_INSTANCE_ID_VALUE = "no-source-application";
        public static final String DEFAULT_SUBJECT_ID_VALUE = "no-subject-id";

        /**
         * A machine-friendly name for the aggregate/entity event message.
         */
        private String eventName;

        /**
         * Aggregate/entity event unique identifier
         */
        private UUID eventId;

        /**
         * Unique identifier for a given instance of the publishing application.
         */
        private String applicationInstanceId;

        /**
         * Aggregate/entity tenant ID
         */
        private String tenantId;

        /**
         * Aggregate/entity asset ID (optional property)
         */
        private String assetId;

        /**
         * Aggregate/entity parent asset ID (optional property)
         */
        private String parentAssetId;

        /**
         * Aggregate/entity name
         */
        @JsonAlias("entityType")
        private String aggregateName;

        /**
         * Aggregate/entity identifier (optional property)
         */
        private String aggregateKey;

        /**
         * The event message creation time.
         */
        private Date createdAt;

        /**
         * Aggregate/entity subject ID
         */
        private String subjectId;

        /**
         * Returns a map (key: String, value: Object) representation of this object.
         *
         * @return a map representation of this object.
         */
        @SuppressWarnings("unchecked")
        public Map<String, Object> toMap() {
            return JsonUtils.objectMapper().convertValue(this, HashMap.class);
        }

    }

    /**
     * Creates a new instance of {@link AssetAggregateEventMessage} using the given {@link
     * AssetAggregateEventEntity}. The type of the payload for the message will always be
     * {@link String}.
     *
     * @param entity The entity to transform into a message.
     * @return A new instance of type {@code AssetAggregateEventMessage<String>}.
     */
    public static AssetAggregateEventMessage<String> of(AssetAggregateEventEntity entity) {
        AssetAggregateEventMessage.Headers headers = new AssetAggregateEventMessage.Headers();
        headers.setEventName(entity.getEventName());
        headers.setEventId(entity.getEventId());
        headers.setApplicationInstanceId(entity.getApplicationInstanceId());
        headers.setTenantId(entity.getTenantId());
        headers.setAssetId(entity.getAssetId());
        headers.setParentAssetId(entity.getParentId());
        headers.setCreatedAt(entity.getCreatedAt());
        headers.setSubjectId(entity.getSubjectId());
        return new AssetAggregateEventMessage<>(headers, entity.getPayload());
    }

}
