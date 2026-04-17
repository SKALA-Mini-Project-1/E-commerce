package com.skala.orderservice.dto;

import com.skala.orderservice.domain.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "주문 상품 응답")
public record OrderItemResponse(
        @Schema(description = "상품 ID", example = "10")
        Long productId,
        @Schema(description = "상품명", example = "Mechanical Keyboard")
        String productName,
        @Schema(description = "주문 시점 단가", example = "129000")
        BigDecimal unitPrice,
        @Schema(description = "수량", example = "2")
        Integer quantity,
        @Schema(description = "소계", example = "258000")
        BigDecimal subtotal
) {
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getProductId(),
                orderItem.getProductName(),
                orderItem.getUnitPrice(),
                orderItem.getQuantity(),
                orderItem.getSubtotal()
        );
    }
}
