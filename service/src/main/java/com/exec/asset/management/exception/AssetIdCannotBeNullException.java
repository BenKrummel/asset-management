package com.exec.asset.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AssetIdCannotBeNullException extends RuntimeException {

    public AssetIdCannotBeNullException() {
        super(String.format("asset-management:asset-id-cannot-be-null: Passed in asset cannot have an id of null"));
    }
}
