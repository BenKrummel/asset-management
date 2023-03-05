package com.exec.asset.management.domain.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAggregateEvent {

    /**
     * Unique identifier for a given instance of the publishing application (optional property).
     */
    private String applicationInstanceId;

    /**
     * Messaging system topic name, where this event message needs to be sent.
     */
    private String msgSysTopic;

    /**
     * Placeholder for additional messaging system headers (optional property).
     */
    private Map<String, Object> msgSysHeaders;

    /**
     * Aggregate/entity asset ID (optional property)
     */
    private String assetId;

    /**
     * Aggregate/entity parent ID (optional property)
     */
    private String parentId;

    /**
     * A machine-friendly name for the aggregate/entity event message.
     */
    private String eventName;

    /**
     * Aggregate/entity aka message payload.
     */
    private Object aggregate;

    /**
     * Aggregate event message partition key (optional property).
     */
    private String partitionKey;

    /**
     * Aggregate event message subject id (optional property).
     */
    private String subjectId;
}
