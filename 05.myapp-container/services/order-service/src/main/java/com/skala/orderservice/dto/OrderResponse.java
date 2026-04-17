package com.skala.orderservice.dto;

import com.skala.orderservice.domain.CustomerOrder;
import com.skala.orderservice.domain.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 응답")
public record OrderResponse(
        @Schema(description = "주문 ID", example = "1001")
        Long id,
        @Schema(description = "주문 번호", example = "ORD-ABC12345")
        String orderNumber,
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "주문 상태")
        OrderStatus status,
        @Schema(description = "총 주문 금액", example = "387000")
        BigDecimal totalAmount,
        @Schema(description = "주문 생성 시각")
        LocalDateTime createdAt,
        @Schema(description = "주문 수정 시각")
        LocalDateTime updatedAt,
        @Schema(description = "주문 상품 목록")
        List<OrderItemResponse> items
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}
