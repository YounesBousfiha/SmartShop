package com.jartiste.smartshop.application.service;

import com.jartiste.smartshop.presentation.dto.request.ProductRequest;
import com.jartiste.smartshop.presentation.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {
    ProductResponse createProduct(ProductRequest request);
    Page<ProductResponse> getAllActiveProduct(Pageable pageable);
    ProductResponse getProductById(Long id);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
}
