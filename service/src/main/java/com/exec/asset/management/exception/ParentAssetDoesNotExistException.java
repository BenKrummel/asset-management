package com.exec.asset.management.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ParentAssetDoesNotExistException extends RuntimeException {

    public ParentAssetDoesNotExistException(UUID id) {
        super(String.format("asset-management:parent-asset-does-not-exist: Parent asset does not exist with id: %s", id.toString()));
    }
}
