package com.shopwave.controller;

import com.shopwave.dto.Dto.CategoryRequest;
import com.shopwave.dto.Dto.CategoryResponse;
import com.shopwave.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Categories")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;

    public CategoryController(CategoryService service) { this.service = service; }

    @Operation(summary = "List categories (public)")
    @GetMapping
    public List<CategoryResponse> list() { return service.list(); }

    @Operation(summary = "Create category (ADMIN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest r) { return service.create(r); }

    @Operation(summary = "Rename category (ADMIN)")
    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest r) { return service.update(id, r); }

    @Operation(summary = "Delete empty category (ADMIN)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
