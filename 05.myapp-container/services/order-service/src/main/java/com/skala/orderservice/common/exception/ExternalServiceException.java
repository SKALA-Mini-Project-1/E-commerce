package com.skala.orderservice.common.exception;

import com.skala.orderservice.common.api.ErrorCode;

public class ExternalServiceException extends BusinessException {

    public ExternalServiceException(String serviceName, String operation) {
        super(ErrorCode.EXTERNAL_SERVICE_ERROR,
                "외부 서비스 호출에 실패했습니다. service=%s, operation=%s".formatted(serviceName, operation));
    }

    public ExternalServiceException(String serviceName, String operation, Throwable cause) {
        super(ErrorCode.EXTERNAL_SERVICE_ERROR,
                "외부 서비스 호출에 실패했습니다. service=%s, operation=%s".formatted(serviceName, operation));
        initCause(cause);
    }
}
