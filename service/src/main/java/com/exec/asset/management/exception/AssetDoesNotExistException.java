package com.exec.asset.management.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AssetDoesNotExistException extends RuntimeException {

    public AssetDoesNotExistException(UUID id) {
        super(String.format("asset-management:asset-does-not-exist: Asset does not exist with id: %s", id.toString()));
    }
}