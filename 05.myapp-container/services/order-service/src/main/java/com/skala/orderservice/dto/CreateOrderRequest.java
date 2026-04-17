package com.skala.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "주문 생성 요청")
public class CreateOrderRequest {

    @NotNull
    @Positive
    @Schema(description = "주문 사용자 ID", example = "1")
    private Long userId;

    @Valid
    @NotEmpty
    @Schema(description = "주문 상품 목록")
    private List<CreateOrderItemRequest> items;
}
