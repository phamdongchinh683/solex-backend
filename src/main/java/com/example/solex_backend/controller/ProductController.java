package com.example.solex_backend.controller;

import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.CreateProductRequest;
import com.example.solex_backend.dto.response.ProductResponse;
import com.example.solex_backend.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Products", description = "Operator product management")
@RestController
@RequestMapping("/api/v1/products")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Create product (operator)")
    @PreAuthorize("hasRole('OPERATOR')")
    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@RequestBody @Valid CreateProductRequest request) {
        return ApiResponse.ok("OK", productService.createProduct(request));
    }

    @Operation(summary = "Get product detail by ID")
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long id) {
        return ApiResponse.ok("OK", productService.getProductById(id));
    }
}
