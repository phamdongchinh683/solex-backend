package com.example.solex_backend.service;

import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.ProductVariant;
import com.example.solex_backend.dto.request.CreateProductVariantRequest;
import com.example.solex_backend.dto.request.UpdateProductVariantRequest;
import com.example.solex_backend.dto.response.ProductVariantResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.ProductRepository;
import com.example.solex_backend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    public ProductVariantResponse createVariant(Long productId, CreateProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (productVariantRepository.findBySku(request.sku()).isPresent()) {
            throw new BusinessException("SKU already exists: " + request.sku());
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(request.sku())
                .size(request.size())
                .price(request.price())
                .stock(request.stock() != null ? request.stock() : 0)
                .imageUrl(request.imageUrl())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        productVariantRepository.save(variant);
        return toResponse(variant);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getVariantsByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        return productVariantRepository.findByProduct(product).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductVariantResponse getVariantById(Long productId, Long variantId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new BusinessException("Variant does not belong to product: " + productId);
        }

        return toResponse(variant);
    }

    public ProductVariantResponse updateVariant(Long productId, Long variantId, UpdateProductVariantRequest request) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new BusinessException("Variant does not belong to product: " + productId);
        }

        if (request.size() != null)     variant.setSize(request.size());
        if (request.price() != null)    variant.setPrice(request.price());
        if (request.stock() != null)    variant.setStock(request.stock());
        if (request.imageUrl() != null) variant.setImageUrl(request.imageUrl());
        if (request.isActive() != null) variant.setIsActive(request.isActive());

        return toResponse(variant);
    }

    public void deleteVariant(Long productId, Long variantId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new BusinessException("Variant does not belong to product: " + productId);
        }

        variant.setIsActive(false);
    }

    private ProductVariantResponse toResponse(ProductVariant v) {
        return new ProductVariantResponse(
                v.getId(), v.getSku(), v.getSize(),
                v.getPrice(), v.getStock(), v.getImageUrl(), v.getIsActive()
        );
    }
}
