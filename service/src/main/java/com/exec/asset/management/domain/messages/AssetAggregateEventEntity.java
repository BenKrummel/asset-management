package com.exec.asset.management.domain.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAggregateEventEntity {

    private UUID eventId;

    private String applicationInstanceId;

    private String msgSysTopic;

    private Map<String, Object> msgSysHeaders;

    private String assetId;

    private String parentId;

    private String eventName;

    private String contentType;

    private String payload;

    private String partitionKey;

    private Date createdAt;

    private String subjectId;

    private String tenantId;
}
