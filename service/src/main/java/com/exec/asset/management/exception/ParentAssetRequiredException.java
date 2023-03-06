package com.exec.asset.management.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ParentAssetRequiredException extends RuntimeException {
    public ParentAssetRequiredException() {
        super(String.format("asset-management:parent-asset-required: Parent asset must be present in the request body"));
    }
}
