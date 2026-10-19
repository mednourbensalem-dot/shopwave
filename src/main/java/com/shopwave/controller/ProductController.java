package com.shopwave.controller;

import com.shopwave.dto.Dto.PageResponse;
import com.shopwave.dto.Dto.ProductRequest;
import com.shopwave.dto.Dto.ProductResponse;
import com.shopwave.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Products")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) { this.service = service; }

    @Operation(summary = "Search the catalog (public): text, category, price range, paging, sorting")
    @GetMapping
    public PageResponse<ProductResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return service.search(q, categoryId, minPrice, maxPrice, page, size, sortBy, direction);
    }

    @Operation(summary = "Product details (public)")
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) { return service.get(id); }

    @Operation(summary = "Create product (ADMIN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest r) { return service.create(r); }

    @Operation(summary = "Update product (ADMIN)")
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest r) { return service.update(id, r); }

    @Operation(summary = "Remove product from catalog - soft delete (ADMIN)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
