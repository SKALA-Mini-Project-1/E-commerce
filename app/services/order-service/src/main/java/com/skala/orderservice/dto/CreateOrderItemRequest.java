package com.skala.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "주문 상품 생성 요청")
public class CreateOrderItemRequest {

    @NotNull
    @Positive
    @Schema(description = "상품 ID", example = "10")
    private Long productId;

    @NotNull
    @Min(1)
    @Schema(description = "주문 수량", example = "2")
    private Integer quantity;
}
