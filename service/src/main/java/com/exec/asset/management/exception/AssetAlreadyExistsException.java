package com.exec.asset.management.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AssetAlreadyExistsException extends RuntimeException {

    public AssetAlreadyExistsException(UUID id) {
        super(String.format("asset-management:asset-already-exists: Asset already exists with id: %s", id.toString()));
    }
}
