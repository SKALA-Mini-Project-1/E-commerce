package com.skala.productservice.dto;

import com.skala.productservice.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "상품 응답")
public record ProductResponse(
        @Schema(description = "상품 ID", example = "1")
        Long id,
        @Schema(description = "상품 SKU", example = "SKU-1001")
        String sku,
        @Schema(description = "상품명", example = "Mechanical Keyboard")
        String name,
        @Schema(description = "상품 설명", example = "87-key keyboard")
        String description,
        @Schema(description = "상품 가격", example = "129000")
        BigDecimal price,
        @Schema(description = "재고 수량", example = "50")
        Integer stockQuantity,
        @Schema(description = "판매 여부", example = "true")
        Boolean active
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getActive()
        );
    }
}
