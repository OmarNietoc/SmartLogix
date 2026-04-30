package com.smartlogix.inventario.controller;

import com.smartlogix.inventario.dto.*; import com.smartlogix.inventario.mapper.ProductMapper; import com.smartlogix.inventario.model.Product; import com.smartlogix.inventario.service.ProductService;
import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/smartlogix/inventory/products") @RequiredArgsConstructor
public class ProductController {
    private final ProductService productService; private final ProductMapper productMapper;
    @GetMapping public ResponseEntity<MessageResponse<List<ProductDTO>>> getAllProducts(@RequestParam(required=false) String companyId) { List<ProductDTO> data=productService.getAllProducts(companyId).stream().map(productMapper::toDto).collect(Collectors.toList()); return ResponseEntity.ok(MessageResponse.<List<ProductDTO>>builder().statusCode(HttpStatus.OK.value()).message("Listado de productos obtenido exitosamente").data(data).build()); }
    @GetMapping("/{id}") public ResponseEntity<MessageResponse<ProductDTO>> getProductById(@PathVariable String id) { return ResponseEntity.ok(MessageResponse.<ProductDTO>builder().statusCode(HttpStatus.OK.value()).message("Producto obtenido exitosamente").data(productMapper.toDto(productService.getProductById(id))).build()); }
    @GetMapping("/sku/{sku}") public ResponseEntity<MessageResponse<ProductDTO>> getProductBySku(@PathVariable String sku) { return ResponseEntity.ok(MessageResponse.<ProductDTO>builder().statusCode(HttpStatus.OK.value()).message("Producto obtenido exitosamente").data(productMapper.toDto(productService.getProductBySku(sku))).build()); }
    @PostMapping public ResponseEntity<MessageResponse<ProductDTO>> createProduct(@RequestBody ProductDTO dto) { Product created=productService.createProduct(productMapper.toEntity(dto)); return new ResponseEntity<>(MessageResponse.<ProductDTO>builder().statusCode(HttpStatus.CREATED.value()).message("Producto creado exitosamente").data(productMapper.toDto(created)).build(), HttpStatus.CREATED); }
    @PutMapping("/{id}") public ResponseEntity<MessageResponse<ProductDTO>> updateProduct(@PathVariable String id, @RequestBody ProductDTO dto) { Product updated=productService.updateProduct(id, productMapper.toEntity(dto)); return ResponseEntity.ok(MessageResponse.<ProductDTO>builder().statusCode(HttpStatus.OK.value()).message("Producto actualizado exitosamente").data(productMapper.toDto(updated)).build()); }
    @DeleteMapping("/{id}") public ResponseEntity<MessageResponse<Void>> deleteProduct(@PathVariable String id) { productService.deleteProduct(id); return ResponseEntity.ok(MessageResponse.<Void>builder().statusCode(HttpStatus.OK.value()).message("Producto eliminado exitosamente").data(null).build()); }
}
