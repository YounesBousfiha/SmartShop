package com.jartiste.smartshop.application.service;


import com.jartiste.smartshop.application.mapper.ProductMapper;
import com.jartiste.smartshop.domain.entity.Product;
import com.jartiste.smartshop.domain.exception.ResourceNotFound;
import com.jartiste.smartshop.domain.repository.ProductRepository;
import com.jartiste.smartshop.presentation.dto.request.ProductRequest;
import com.jartiste.smartshop.presentation.dto.response.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductRepository productRepository;

    private  Product toDelete;

    @BeforeEach
    void setup() {
         toDelete = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .stock(20)
                .deleted(false)
                .price(BigDecimal.valueOf(30000))
                .build();

        productRepository.save(toDelete);
    }

    @Test
    @DisplayName("Should Create new Product")
    void shouldCreateProduct() {
        ProductRequest request = new ProductRequest(
                "Dell Latitude",
                BigDecimal.valueOf(4500),
                12
        );

        Product product = Product.builder()
                .name("Dell Latitude")
                .price(BigDecimal.valueOf(4500))
                .stock(12)
                .deleted(false)
                .build();

        Product savedProduct = Product.builder()
                .name("Dell Latitude")
                .price(BigDecimal.valueOf(4500))
                .stock(12)
                .deleted(false)
                .build();

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(savedProduct);
        when(productMapper.toResponse(savedProduct)).thenReturn(
                new ProductResponse(1L, "Dell Latitude", BigDecimal.valueOf(4500), 12, false)
        );

        ProductResponse response = productService.createProduct(request);


        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Dell Latitude", response.name());
        assertEquals(BigDecimal.valueOf(4500), response.price());
        assertEquals(12, response.stock());
    }

    @Test
    @DisplayName("Should Get Product By ID")
    void shouldGetProductById() {
        Product product = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .price(BigDecimal.valueOf(30000))
                .stock(20)
                .deleted(false)
                .build();

        ProductResponse expectedResponse = new ProductResponse(
                1L,
                "MacBook Pro",
                BigDecimal.valueOf(30000),
                20,
                false
        );

        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(expectedResponse);

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("MacBook Pro", response.name());
        assertEquals(BigDecimal.valueOf(30000), response.price());
        assertEquals(20, response.stock());
        assertEquals(false, response.deleted());
    }

    @Test
    @DisplayName("Should Update Product")
    void shouldUpdateProduct() {
        Product product = Product.builder()
                .id(1L)
                .name("Cable VGA")
                .price(BigDecimal.valueOf(50))
                .stock(20)
                .deleted(false)
                .build();

        ProductRequest request = new ProductRequest(
                "Cable HDMI", null, null
        );

        ProductResponse expectedResponse = ProductResponse.builder()
                .id(1L)
                .name("Cable HDMI")
                .price(BigDecimal.valueOf(50))
                .stock(20)
                .deleted(false)
                .build();

        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(
                new ProductResponse(1L, "Cable HDMI", BigDecimal.valueOf(50), 20, false)
        );

        ProductResponse response = productService.updateProduct(1L, request);

        assertNotNull(response);
        assertEquals(expectedResponse.id(), response.id());
        assertEquals(expectedResponse.name(), response.name());
        assertEquals(expectedResponse.price(), response.price());
        assertEquals(expectedResponse.stock(), response.stock());
    }

    @Test
    void shouldDeleteProduct() {

        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(toDelete));
        when(productRepository.save(toDelete)).thenReturn((toDelete));

        productService.deleteProduct(1L);

        assertTrue(toDelete.getDeleted());
    }

    @Test
    @DisplayName("Should get All Active Product")
    void shouldGetAllActiveProduct() {
        Pageable pageable = PageRequest.of(0, 10);

        Product product1 = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .price(BigDecimal.valueOf(30000))
                .stock(20)
                .deleted(false)
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Dell Latitude")
                .price(BigDecimal.valueOf(4500))
                .stock(12)
                .deleted(false)
                .build();


        List<Product> productList = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(productList, pageable, productList.size());

        ProductResponse response1 = new ProductResponse(
                1L, "MacBook Pro", BigDecimal.valueOf(30000), 20, false
        );

        ProductResponse response2 = new ProductResponse(
                2L, "Dell Latitude", BigDecimal.valueOf(4500), 12, false
        );

        when(productRepository.findAllByDeletedFalse(pageable)).thenReturn(productPage);
        when(productMapper.toResponse(product1)).thenReturn(response1);
        when(productMapper.toResponse(product2)).thenReturn(response2);

        Page<ProductResponse> results = productService.getAllActiveProduct(pageable);

        assertNotNull(results);
        assertEquals(2, results.getTotalElements());
        assertEquals(2, results.getContent().size());
        assertEquals("MacBook Pro", results.getContent().get(0).name());

    }


    @Test
    @DisplayName("Should throw Exception  if product non-exist")
    void shouldThrowExceptionWhenMakeOperationToNonExistentProduct() {
        when(productRepository.findByIdAndDeletedFalse(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> {
            productService.deleteProduct(99L);
        });
    }
}
