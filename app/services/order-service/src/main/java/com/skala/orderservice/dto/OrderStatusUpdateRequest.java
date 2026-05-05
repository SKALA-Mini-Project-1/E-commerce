package com.skala.orderservice.dto;

import com.skala.orderservice.domain.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "주문 상태 변경 요청")
public class OrderStatusUpdateRequest {

    @NotNull
    @Schema(description = "변경할 주문 상태", example = "PAID")
    private OrderStatus status;
}
