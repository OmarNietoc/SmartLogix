package com.smartlogix.inventory.controller;

import com.smartlogix.inventory.dto.*;
import com.smartlogix.inventory.mapper.ProductMapper;
import com.smartlogix.inventory.model.Product;
import com.smartlogix.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Inventory - Productos", description = "Catálogo de productos por empresa")
@RestController
@RequestMapping("/smartlogix/inventory/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductMapper productMapper;

    @Operation(summary = "Listar productos", description = "Retorna todos los productos. Filtrable por empresa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<MessageResponse<List<ProductDTO>>> getAllProducts(
            @Parameter(description = "UUID de la empresa") @RequestHeader("X-Company-Id") String companyId) {
        List<ProductDTO> data = productService.getAllProducts(companyId).stream()
                .map(productMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(MessageResponse.<List<ProductDTO>>builder()
                .statusCode(HttpStatus.OK.value()).message("Listado de productos obtenido exitosamente").data(data).build());
    }

    @Operation(summary = "Obtener producto por ID", description = "Retorna un producto por su UUID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<ProductDTO>> getProductById(
            @Parameter(description = "UUID del producto") @PathVariable String id) {
        return ResponseEntity.ok(MessageResponse.<ProductDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Producto obtenido exitosamente")
                .data(productMapper.toDto(productService.getProductById(id))).build());
    }

    @Operation(summary = "Obtener producto por SKU", description = "Retorna un producto por su código SKU único")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/sku/{sku}")
    public ResponseEntity<MessageResponse<ProductDTO>> getProductBySku(
            @Parameter(description = "Código SKU del producto", example = "PROD-001") @PathVariable String sku) {
        return ResponseEntity.ok(MessageResponse.<ProductDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Producto obtenido exitosamente")
                .data(productMapper.toDto(productService.getProductBySku(sku))).build());
    }

    @Operation(summary = "Crear producto", description = "Registra un nuevo producto en el catálogo")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o SKU duplicado")
    })
    @PostMapping
    public ResponseEntity<MessageResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO dto) {
        Product created = productService.createProduct(productMapper.toEntity(dto));
        return new ResponseEntity<>(MessageResponse.<ProductDTO>builder()
                .statusCode(HttpStatus.CREATED.value()).message("Producto creado exitosamente")
                .data(productMapper.toDto(created)).build(), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar producto", description = "Modifica los datos de un producto existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse<ProductDTO>> updateProduct(
            @Parameter(description = "UUID del producto") @PathVariable String id,
            @Valid @RequestBody ProductDTO dto) {
        Product updated = productService.updateProduct(id, productMapper.toEntity(dto));
        return ResponseEntity.ok(MessageResponse.<ProductDTO>builder()
                .statusCode(HttpStatus.OK.value()).message("Producto actualizado exitosamente")
                .data(productMapper.toDto(updated)).build());
    }

    @Operation(summary = "Eliminar producto", description = "Elimina un producto del catálogo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse<Void>> deleteProduct(
            @Parameter(description = "UUID del producto") @PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(MessageResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value()).message("Producto eliminado exitosamente").data(null).build());
    }
}
