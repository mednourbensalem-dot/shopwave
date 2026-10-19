package com.shopwave.service;

import com.shopwave.dto.Dto.CategoryRequest;
import com.shopwave.dto.Dto.CategoryResponse;
import com.shopwave.exception.ApiException;
import com.shopwave.model.Category;
import com.shopwave.repo.CategoryRepository;
import com.shopwave.repo.ProductRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categories;
    private final ProductRepository products;

    public CategoryService(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    private CategoryResponse map(Category c) { return new CategoryResponse(c.getId(), c.getName()); }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() { return categories.findAll().stream().map(this::map).toList(); }

    public CategoryResponse create(CategoryRequest r) {
        if (categories.existsByNameIgnoreCase(r.name().trim())) throw new ApiException(HttpStatus.CONFLICT, "Category exists");
        Category c = new Category();
        c.setName(r.name().trim());
        return map(categories.save(c));
    }

    public CategoryResponse update(Long id, CategoryRequest r) {
        Category c = categories.findById(id).orElseThrow(() -> ApiException.notFound("Category"));
        c.setName(r.name().trim());
        return map(c);
    }

    public void delete(Long id) {
        if (!categories.existsById(id)) throw ApiException.notFound("Category");
        if (products.existsByCategoryIdAndActiveTrue(id)) throw new ApiException(HttpStatus.CONFLICT, "Category still has products");
        categories.deleteById(id);
    }
}
