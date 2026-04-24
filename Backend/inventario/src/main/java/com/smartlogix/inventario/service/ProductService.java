package com.smartlogix.inventario.service;

import com.smartlogix.inventario.model.Product;
import com.smartlogix.inventario.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Optional<Product> obtenerPorSku(String sku) {
        return productRepository.findBySku(sku);
    }

    public Product crearProducto(Product producto) {
        if (producto.getSku() == null || producto.getSku().isEmpty()) {
            throw new IllegalArgumentException("El SKU es obligatorio para SmartLogix");
        }
        return productRepository.save(producto);
    }

    // Nota: La actualización de stock ahora debería ser sobre la tabla
    // inventory_stock si quieres manejar múltiples bodegas,
    // pero para empezar, usemos este método simple:
    public Product actualizarStockBasico(Long id, Integer nuevaCantidad) {
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        return productRepository.findById(id).map(p -> {
            // Aquí podrías agregar lógica de historial de movimientos después
            return productRepository.save(p);
        }).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }
}