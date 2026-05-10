package com.smartlogix.inventory.service;

import com.smartlogix.inventory.exception.ProductNotFoundException;
import com.smartlogix.inventory.model.Product;
import com.smartlogix.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getAllProducts(String companyId) {
        requireCompanyId(companyId);
        return productRepository.findByCompanyId(companyId);
    }

    public Product getProductById(String id, String companyId) {
        requireCompanyId(companyId);
        return productRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ProductNotFoundException("El producto con ID " + id + " no fue encontrado."));
    }

    public Product getProductBySku(String sku, String companyId) {
        requireCompanyId(companyId);
        return productRepository.findBySkuAndCompanyId(sku, companyId)
                .orElseThrow(() -> new ProductNotFoundException("El producto con SKU " + sku + " no fue encontrado."));
    }

    @Transactional
    public Product createProduct(Product product, String companyId) {
        requireCompanyId(companyId);
        product.setCompanyId(companyId);
        validateProduct(product);
        if (productRepository.existsBySkuAndCompanyId(product.getSku(), companyId)) {
            throw new IllegalStateException("Ya existe un producto con el SKU " + product.getSku());
        }
        log.info("Creando producto SKU {} para compania {}", product.getSku(), product.getCompanyId());
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(String id, Product product, String companyId) {
        Product existing = getProductById(id, companyId);
        product.setCompanyId(companyId);
        validateProduct(product);
        if (!existing.getSku().equals(product.getSku()) && productRepository.existsBySkuAndCompanyId(product.getSku(), companyId)) {
            throw new IllegalStateException("Ya existe un producto con el SKU " + product.getSku());
        }
        existing.setSku(product.getSku());
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        return productRepository.save(existing);
    }

    @Transactional
    public void deleteProduct(String id, String companyId) {
        Product product = getProductById(id, companyId);
        product.setStatus("INACTIVE");
        productRepository.save(product);
    }

    private void requireCompanyId(String companyId) {
        if (companyId == null || companyId.isBlank()) {
            throw new IllegalArgumentException("X-Company-Id es obligatorio");
        }
    }

    private void validateProduct(Product product) {
        if (product.getCompanyId() == null || product.getCompanyId().isBlank()) {
            throw new IllegalArgumentException("companyId es obligatorio");
        }
        if (product.getSku() == null || product.getSku().isBlank()) {
            throw new IllegalArgumentException("sku es obligatorio");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("name es obligatorio");
        }
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            throw new IllegalArgumentException("price debe ser mayor o igual a cero");
        }
    }
}
