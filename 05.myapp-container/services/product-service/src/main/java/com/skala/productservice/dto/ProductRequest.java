package com.skala.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "상품 생성/수정 요청")
public class ProductRequest {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "상품 SKU", example = "SKU-1001")
    private String sku;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "상품명", example = "Mechanical Keyboard")
    private String name;

    @Size(max = 500)
    @Schema(description = "상품 설명", example = "87-key keyboard")
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Schema(description = "상품 가격", example = "129000")
    private BigDecimal price;

    @NotNull
    @Min(0)
    @Schema(description = "재고 수량", example = "50")
    private Integer stockQuantity;

    @Schema(description = "판매 여부", example = "true")
    private Boolean active;
}
