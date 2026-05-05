package com.skala.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "재고 복구 요청")
public class ProductStockRestoreRequest {

    @NotNull
    @Min(1)
    @Schema(description = "복구 수량", example = "2")
    private Integer quantity;
}
