package com.exec.asset.management.exception;

import java.util.UUID;

public class MismatchedIds extends RuntimeException {

    public MismatchedIds(UUID modelId, UUID paramId) {
        super(String.format("asset-management:mismatched-id: Mismatched ids body id: %s parameter id: %s", modelId.toString(), paramId.toString()));
    }
}
