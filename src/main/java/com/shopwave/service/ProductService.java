package com.shopwave.service;

import com.shopwave.dto.Dto.PageResponse;
import com.shopwave.dto.Dto.ProductRequest;
import com.shopwave.dto.Dto.ProductResponse;
import com.shopwave.dto.Mapper;
import com.shopwave.exception.ApiException;
import com.shopwave.model.Category;
import com.shopwave.model.Product;
import com.shopwave.repo.CategoryRepository;
import com.shopwave.repo.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;

    public ProductService(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String q, Long categoryId, BigDecimal min, BigDecimal max,
                                                int page, int size, String sortBy, String dir) {
        String field = List.of("id", "name", "price").contains(sortBy) ? sortBy : "id";
        Sort sort = "desc".equalsIgnoreCase(dir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        PageRequest pg = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);
        Page<Product> r = products.search(q == null ? "" : q.trim(), categoryId == null ? 0L : categoryId,
                min == null ? BigDecimal.ZERO : min, max == null ? new BigDecimal("999999999") : max, pg);
        return new PageResponse<>(r.getContent().stream().map(Mapper::product).toList(),
                r.getNumber(), r.getSize(), r.getTotalElements(), r.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return Mapper.product(find(id));
    }

    public ProductResponse create(ProductRequest r) {
        Product p = new Product();
        apply(p, r);
        return Mapper.product(products.save(p));
    }

    public ProductResponse update(Long id, ProductRequest r) {
        Product p = find(id);
        apply(p, r);
        return Mapper.product(p);
    }

    /** Soft delete: product disappears from the catalog but old orders stay valid. */
    public void delete(Long id) {
        find(id).setActive(false);
    }

    private Product find(Long id) {
        return products.findById(id).filter(Product::isActive).orElseThrow(() -> ApiException.notFound("Product"));
    }

    private void apply(Product p, ProductRequest r) {
        p.setName(r.name().trim());
        p.setDescription(r.description());
        p.setPrice(r.price());
        p.setStock(r.stock());
        p.setImageUrl(r.imageUrl());
        Category c = null;
        if (r.categoryId() != null) c = categories.findById(r.categoryId()).orElseThrow(() -> ApiException.notFound("Category"));
        p.setCategory(c);
    }
}
