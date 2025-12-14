package com.jartiste.smartshop.application.service.impl;

import com.jartiste.smartshop.application.mapper.ProductMapper;
import com.jartiste.smartshop.application.service.IProductService;
import com.jartiste.smartshop.domain.entity.Product;
import com.jartiste.smartshop.domain.exception.ResourceNotFound;
import com.jartiste.smartshop.domain.repository.ProductRepository;
import com.jartiste.smartshop.presentation.dto.request.ProductRequest;
import com.jartiste.smartshop.presentation.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private static final String NOT_FOUND = "Product not Found";

    public ProductResponse createProduct(ProductRequest request) {
        Product product = productMapper.toEntity(request);

        Product newProduct = this.productRepository.save(product);

        return this.productMapper.toResponse(newProduct);
    }

    public Page<ProductResponse> getAllActiveProduct(Pageable pageable) {
        return this.productRepository.findAllByDeletedFalse(pageable)
                .map(productMapper::toResponse);
    }

    public ProductResponse getProductById(Long id) {
        return this.productRepository.findByIdAndDeletedFalse(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFound(NOT_FOUND));
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = this.productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFound(NOT_FOUND));

        productMapper.updateProductFromDto(request, product);

        Product updatedProduct = this.productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    public void deleteProduct(Long id) {
        Product product = this.productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFound(NOT_FOUND));

        product.setDeleted(true);
        this.productRepository.save(product);
    }
}
