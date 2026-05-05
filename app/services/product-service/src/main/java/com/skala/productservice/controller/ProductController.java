package com.skala.productservice.controller;

import com.skala.productservice.common.api.ErrorResponse;
import com.skala.productservice.common.exception.ResourceNotFoundException;
import com.skala.productservice.dto.ProductRequest;
import com.skala.productservice.dto.ProductResponse;
import com.skala.productservice.dto.ProductStockDeductRequest;
import com.skala.productservice.dto.ProductStockRestoreRequest;
import com.skala.productservice.dto.ProductStockUpdateRequest;
import com.skala.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Tag(name = "Products", description = "상품 및 재고 관리 API")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 목록 조회", description = "이름 또는 활성 여부로 상품을 조회합니다.")
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getProducts(@RequestParam Optional<String> name,
                                                             @RequestParam Optional<Boolean> active) {
        return ResponseEntity.ok(productService.findAll(name, active).stream()
                .map(ProductResponse::from)
                .toList());
    }

    @Operation(summary = "상품 단건 조회")
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable @Positive Long id) {
        return productService.findById(id)
                .map(ProductResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다: " + id));
    }

    @Operation(summary = "SKU 기준 상품 조회")
    @GetMapping("/products/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        return productService.findBySku(sku)
                .map(ProductResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다. sku=" + sku));
    }

    @Operation(summary = "상품 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "상품 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(ProductResponse.from(productService.create(request)), HttpStatus.CREATED);
    }

    @Operation(summary = "상품 수정")
    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable @Positive Long id,
                                                         @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ProductResponse.from(productService.update(id, request)));
    }

    @Operation(summary = "재고 직접 수정")
    @PatchMapping("/products/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(@PathVariable @Positive Long id,
                                                       @Valid @RequestBody ProductStockUpdateRequest request) {
        return ResponseEntity.ok(ProductResponse.from(productService.updateStock(id, request.getStockQuantity())));
    }

    @Operation(summary = "재고 차감")
    @PostMapping("/products/{id}/deduct-stock")
    public ResponseEntity<ProductResponse> deductStock(@PathVariable @Positive Long id,
                                                       @Valid @RequestBody ProductStockDeductRequest request) {
        return ResponseEntity.ok(ProductResponse.from(productService.deductStock(id, request.getQuantity())));
    }

    @Operation(summary = "재고 복구")
    @PostMapping("/products/{id}/restore-stock")
    public ResponseEntity<ProductResponse> restoreStock(@PathVariable @Positive Long id,
                                                        @Valid @RequestBody ProductStockRestoreRequest request) {
        return ResponseEntity.ok(ProductResponse.from(productService.restoreStock(id, request.getQuantity())));
    }

    @Operation(summary = "상품 삭제")
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @Positive Long id) {
        if (!productService.delete(id)) {
            throw new ResourceNotFoundException("상품을 찾을 수 없습니다: " + id);
        }
        return ResponseEntity.noContent().build();
    }
}
