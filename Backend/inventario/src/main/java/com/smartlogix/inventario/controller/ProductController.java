package com.smartlogix.inventario.controller;

import com.smartlogix.inventario.model.Product;
import com.smartlogix.inventario.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/sku/{sku}")
    public ResponseEntity<Product> obtenerPorSku(@PathVariable String sku) {
        return productService.obtenerPorSku(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearProducto(@RequestBody Product producto) {
        try {
            return ResponseEntity.ok(productService.crearProducto(producto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}