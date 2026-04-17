package com.skala.productservice.common.exception;

import com.skala.productservice.common.api.ErrorCode;
import com.skala.productservice.common.api.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception,
                                                          HttpServletRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return buildResponse(ErrorCode.VALIDATION_ERROR, "입력값 검증에 실패했습니다.", request, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception,
                                                                   HttpServletRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getConstraintViolations()
                .forEach(violation -> details.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return buildResponse(ErrorCode.VALIDATION_ERROR, "입력값 검증에 실패했습니다.", request, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException exception,
                                                           HttpServletRequest request) {
        return buildResponse(ErrorCode.VALIDATION_ERROR, "요청 본문을 해석할 수 없습니다.", request, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException exception, HttpServletRequest request) {
        return buildResponse(exception.getErrorCode(), exception.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", request, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode,
                                                        String message,
                                                        HttpServletRequest request,
                                                        Map<String, String> details) {
        HttpStatus status = errorCode.getStatus();
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorCode.name(),
                message,
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(status).body(response);
    }
}
