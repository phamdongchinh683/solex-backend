package com.example.solex_backend.service;

import com.example.solex_backend.domain.Product;
import com.example.solex_backend.domain.ProductVariant;
import com.example.solex_backend.dto.request.CreateProductVariantRequest;
import com.example.solex_backend.dto.request.UpdateProductVariantRequest;
import com.example.solex_backend.dto.response.ProductVariantResponse;
import com.example.solex_backend.dto.response.VariantResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.ProductRepository;
import com.example.solex_backend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    public ProductVariantResponse createVariant(Long productId, CreateProductVariantRequest request) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        Product product = productRepository.getReferenceById(productId);

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(request.sku())
                .price(request.price())
                .image(request.image())
                .size(request.size())
                .name(request.name())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        productVariantRepository.save(variant);
        return toResponse(variant);
    }

    @Transactional(readOnly = true)
    public ProductVariantResponse getVariantById(Long productId, Long variantId) {
        ProductVariant variant = productVariantRepository.findByIdAndProduct_Id(variantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));
        return toResponse(variant);
    }

    public ProductVariantResponse updateVariant(Long productId, Long variantId, UpdateProductVariantRequest request) {
        ProductVariant variant = productVariantRepository.findByIdAndProduct_Id(variantId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));

        if (request.price() != null)
            variant.setPrice(request.price());
        if (request.image() != null)
            variant.setImage(request.image());
        if (request.size() != null)
            variant.setSize(request.size());
        if (request.name() != null)
            variant.setName(request.name());
        if (request.isActive() != null)
            variant.setIsActive(request.isActive());

        productVariantRepository.save(variant);
        return toResponse(variant);
    }

    public void deleteVariant(Long productId, Long variantId) {
        if (!productVariantRepository.existsByIdAndProduct_Id(variantId, productId)) {
            throw new ResourceNotFoundException("Variant not found: " + variantId);
        }
        productVariantRepository.deactivateById(variantId);
    }

    @Transactional(readOnly = true)
    public List<VariantResponse> findAllByProductionId(Long productId) {
        List<ProductVariant> result = productVariantRepository.findVariantsByProductId(productId);
        return result.stream()
                .map(v -> new VariantResponse(v.getId(), v.getPrice(), v.getName(), v.getSize(), v.getImage()))
                .toList();
    }

    private ProductVariantResponse toResponse(ProductVariant v) {
        return new ProductVariantResponse(
                v.getId(), v.getSku(),
                v.getPrice(), v.getImage(), v.getSize(), v.getName(), v.getIsActive());
    }
}
