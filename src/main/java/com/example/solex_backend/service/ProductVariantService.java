package com.example.solex_backend.service;

import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.ProductVariant;
import com.example.solex_backend.dto.request.CreateProductVariantRequest;
import com.example.solex_backend.dto.request.UpdateProductVariantRequest;
import com.example.solex_backend.dto.response.ProductVariantResponse;
import com.example.solex_backend.dto.response.SliceResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.ProductRepository;
import com.example.solex_backend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    // Rule 2: getReferenceById replaces findById used only for FK assignment
    public ProductVariantResponse createVariant(Long productId, CreateProductVariantRequest request) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        if (productVariantRepository.existsBySku(request.sku())) {
            throw new BusinessException("SKU already exists: " + request.sku());
        }

        Product product = productRepository.getReferenceById(productId);

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
    public SliceResponse<ProductVariantResponse> getVariantsByProduct(Long productId, Long cursor, int size) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        List<ProductVariant> result = productVariantRepository.findByProductAfterCursor(productId, cursor, PageRequest.of(0, size + 1));
        boolean hasNext = result.size() > size;
        List<ProductVariant> page = hasNext ? result.subList(0, size) : result;
        Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;
        return new SliceResponse<>(page.stream().map(this::toResponse).toList(), nextCursor);
    }

    // Rule 1: findByIdAndProduct_Id combines existence + ownership check in one query
    @Transactional(readOnly = true)
    public ProductVariantResponse getVariantById(Long productId, Long variantId) {
        ProductVariant variant = productVariantRepository.findByIdAndProduct_Id(variantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));
        return toResponse(variant);
    }

    // Rule 1: findByIdAndProduct_Id replaces findById + manual ID comparison
    public ProductVariantResponse updateVariant(Long productId, Long variantId, UpdateProductVariantRequest request) {
        ProductVariant variant = productVariantRepository.findByIdAndProduct_Id(variantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));

        if (request.size() != null)     variant.setSize(request.size());
        if (request.price() != null)    variant.setPrice(request.price());
        if (request.stock() != null)    variant.setStock(request.stock());
        if (request.imageUrl() != null) variant.setImageUrl(request.imageUrl());
        if (request.isActive() != null) variant.setIsActive(request.isActive());

        productVariantRepository.save(variant);
        return toResponse(variant);
    }

    // Rule 1: existsByIdAndProduct_Id replaces findById + manual ID comparison
    // Rule 3: deactivateById @Modifying UPDATE replaces load → setIsActive(false) → dirty check
    public void deleteVariant(Long productId, Long variantId) {
        if (!productVariantRepository.existsByIdAndProduct_Id(variantId, productId)) {
            throw new ResourceNotFoundException("Variant not found: " + variantId);
        }
        productVariantRepository.deactivateById(variantId);
    }

    private ProductVariantResponse toResponse(ProductVariant v) {
        return new ProductVariantResponse(
                v.getId(), v.getSku(), v.getSize(),
                v.getPrice(), v.getStock(), v.getImageUrl(), v.getIsActive()
        );
    }
}
