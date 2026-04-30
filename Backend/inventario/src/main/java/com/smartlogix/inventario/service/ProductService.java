package com.smartlogix.inventario.service;

import com.smartlogix.inventario.exception.ProductNotFoundException;
import com.smartlogix.inventario.model.Product;
import com.smartlogix.inventario.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j @Service @RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getAllProducts(String companyId) {
        return companyId != null ? productRepository.findByCompanyId(companyId) : productRepository.findAll();
    }
    public Product getProductById(String id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("El producto con ID " + id + " no fue encontrado."));
    }
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku).orElseThrow(() -> new ProductNotFoundException("El producto con SKU " + sku + " no fue encontrado."));
    }
    @Transactional
    public Product createProduct(Product product) {
        validateProduct(product);
        if (productRepository.existsBySku(product.getSku())) throw new IllegalStateException("Ya existe un producto con el SKU " + product.getSku());
        log.info("Creando producto SKU {} para compañía {}", product.getSku(), product.getCompanyId());
        return productRepository.save(product);
    }
    @Transactional
    public Product updateProduct(String id, Product product) {
        Product existing = getProductById(id);
        validateProduct(product);
        existing.setCompanyId(product.getCompanyId()); existing.setSku(product.getSku()); existing.setName(product.getName()); existing.setPrice(product.getPrice());
        return productRepository.save(existing);
    }
    @Transactional public void deleteProduct(String id) { productRepository.delete(getProductById(id)); }
    private void validateProduct(Product product) {
        if (product.getCompanyId() == null || product.getCompanyId().isBlank()) throw new IllegalArgumentException("companyId es obligatorio");
        if (product.getSku() == null || product.getSku().isBlank()) throw new IllegalArgumentException("sku es obligatorio");
        if (product.getName() == null || product.getName().isBlank()) throw new IllegalArgumentException("name es obligatorio");
        if (product.getPrice() == null || product.getPrice().signum() < 0) throw new IllegalArgumentException("price debe ser mayor o igual a cero");
    }
}
