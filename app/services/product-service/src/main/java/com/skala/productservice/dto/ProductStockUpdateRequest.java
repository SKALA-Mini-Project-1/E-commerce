package com.skala.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "재고 직접 수정 요청")
public class ProductStockUpdateRequest {

    @NotNull
    @Min(0)
    @Schema(description = "변경할 재고 수량", example = "120")
    private Integer stockQuantity;
}
