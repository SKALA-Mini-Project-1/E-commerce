package com.skala.paymentservice.client;

import com.skala.paymentservice.common.api.ErrorCode;
import com.skala.paymentservice.common.exception.BusinessException;
import com.skala.paymentservice.common.exception.ExternalServiceException;
import com.skala.paymentservice.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderClient {

    private final RestTemplate restTemplate;

    @Value("${clients.order-service.base-url}")
    private String orderServiceBaseUrl;

    public OrderSummary getOrder(Long orderId) {
        try {
            OrderSummary order = restTemplate.getForObject(
                    orderServiceBaseUrl + "/api/orders/" + orderId,
                    OrderSummary.class
            );
            if (order == null) {
                throw new ResourceNotFoundException("주문을 찾을 수 없습니다: " + orderId);
            }
            return order;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("주문을 찾을 수 없습니다: " + orderId);
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException("order-service", "get-order", e);
        } catch (RestClientException e) {
            throw new ExternalServiceException("order-service", "get-order", e);
        }
    }

    public void markOrderPaid(Long orderId) {
        try {
            restTemplate.exchange(
                    orderServiceBaseUrl + "/api/orders/" + orderId + "/status",
                    HttpMethod.PATCH,
                    new HttpEntity<>(new OrderStatusUpdateRequest("PAID")),
                    Void.class
            );
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("주문을 찾을 수 없습니다: " + orderId);
        } catch (HttpClientErrorException e) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "주문 상태 변경에 실패했습니다: " + orderId);
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException("order-service", "mark-order-paid", e);
        } catch (RestClientException e) {
            throw new ExternalServiceException("order-service", "mark-order-paid", e);
        }
    }

    public void cancelOrder(Long orderId) {
        try {
            restTemplate.postForEntity(
                    orderServiceBaseUrl + "/api/orders/" + orderId + "/cancel",
                    HttpEntity.EMPTY,
                    Void.class
            );
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("주문을 찾을 수 없습니다: " + orderId);
        } catch (HttpClientErrorException e) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "주문 취소에 실패했습니다: " + orderId);
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException("order-service", "cancel-order", e);
        } catch (RestClientException e) {
            throw new ExternalServiceException("order-service", "cancel-order", e);
        }
    }

    public record OrderStatusUpdateRequest(String status) {
    }

    public record OrderSummary(
            Long id,
            String orderNumber,
            Long userId,
            String status,
            BigDecimal totalAmount,
            List<OrderItemSummary> items
    ) {
    }

    public record OrderItemSummary(
            Long productId,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal subtotal
    ) {
    }
}
