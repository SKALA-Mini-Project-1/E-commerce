package com.skala.orderservice.client;

import com.skala.orderservice.common.api.ErrorCode;
import com.skala.orderservice.common.exception.BusinessException;
import com.skala.orderservice.common.exception.ExternalServiceException;
import com.skala.orderservice.common.exception.ResourceNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${clients.product-service.base-url}")
    private String productServiceBaseUrl;

    public ProductSummary getProduct(Long productId) {
        try {
            ProductSummary product = restTemplate.getForObject(
                    productServiceBaseUrl + "/api/products/" + productId,
                    ProductSummary.class
            );
            if (product == null) {
                throw new ResourceNotFoundException("상품을 찾을 수 없습니다: " + productId);
            }
            return product;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("상품을 찾을 수 없습니다: " + productId);
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException("product-service", "get-product", e);
        } catch (RestClientException e) {
            throw new ExternalServiceException("product-service", "get-product", e);
        }
    }

    public void deductStock(Long productId, Integer quantity) {
        try {
            restTemplate.postForEntity(
                    productServiceBaseUrl + "/api/products/" + productId + "/deduct-stock",
                    new ProductStockDeductRequest(quantity),
                    Void.class
            );
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("상품을 찾을 수 없습니다: " + productId);
        } catch (HttpClientErrorException e) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "상품 재고 차감에 실패했습니다: " + productId);
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException("product-service", "deduct-stock", e);
        } catch (RestClientException e) {
            throw new ExternalServiceException("product-service", "deduct-stock", e);
        }
    }

    public void restoreStock(Long productId, Integer quantity) {
        try {
            restTemplate.postForEntity(
                    productServiceBaseUrl + "/api/products/" + productId + "/restore-stock",
                    new ProductStockRestoreRequest(quantity),
                    Void.class
            );
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("상품을 찾을 수 없습니다: " + productId);
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException("product-service", "restore-stock", e);
        } catch (RestClientException e) {
            throw new ExternalServiceException("product-service", "restore-stock", e);
        }
    }

    @Getter
    @Setter
    public static class ProductSummary {
        private Long id;
        private String sku;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
        private Boolean active;
    }

    public record ProductStockDeductRequest(Integer quantity) {
    }

    public record ProductStockRestoreRequest(Integer quantity) {
    }
}
