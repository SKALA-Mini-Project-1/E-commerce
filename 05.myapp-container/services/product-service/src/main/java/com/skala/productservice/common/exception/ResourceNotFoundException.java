package com.skala.productservice.common.exception;

import com.skala.productservice.common.api.ErrorCode;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
