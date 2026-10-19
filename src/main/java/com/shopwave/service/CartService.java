package com.shopwave.service;

import com.shopwave.dto.Dto.CartItemRequest;
import com.shopwave.dto.Dto.CartItemResponse;
import com.shopwave.dto.Dto.CartResponse;
import com.shopwave.dto.Mapper;
import com.shopwave.exception.ApiException;
import com.shopwave.model.AppUser;
import com.shopwave.model.CartItem;
import com.shopwave.model.Product;
import com.shopwave.repo.AppUserRepository;
import com.shopwave.repo.CartItemRepository;
import com.shopwave.repo.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {
    private final CartItemRepository items;
    private final ProductRepository products;
    private final AppUserRepository users;

    public CartService(CartItemRepository items, ProductRepository products, AppUserRepository users) {
        this.items = items;
        this.products = products;
        this.users = users;
    }

    private AppUser user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }

    private Product activeProduct(Long id) {
        return products.findById(id).filter(Product::isActive).orElseThrow(() -> ApiException.notFound("Product"));
    }

    private void checkStock(Product p, int qty) {
        if (qty > p.getStock()) throw new ApiException(HttpStatus.CONFLICT, "Only " + p.getStock() + " in stock for " + p.getName());
    }

    private CartResponse toResponse(List<CartItem> list) {
        List<CartItemResponse> rows = list.stream().map(Mapper::cartItem).toList();
        BigDecimal total = rows.stream().map(CartItemResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(rows, total);
    }

    @Transactional(readOnly = true)
    public CartResponse get(String email) {
        return toResponse(items.findByUser(user(email)));
    }

    /** Adds quantity to the existing line (or creates it). */
    public CartResponse add(String email, CartItemRequest r) {
        AppUser u = user(email);
        Product p = activeProduct(r.productId());
        CartItem ci = items.findByUserAndProduct(u, p).orElseGet(() -> {
            CartItem c = new CartItem();
            c.setUser(u);
            c.setProduct(p);
            return c;
        });
        int qty = ci.getQuantity() + r.quantity();
        checkStock(p, qty);
        ci.setQuantity(qty);
        items.save(ci);
        return toResponse(items.findByUser(u));
    }

    /** Sets the exact quantity of a line. */
    public CartResponse update(String email, CartItemRequest r) {
        AppUser u = user(email);
        Product p = activeProduct(r.productId());
        CartItem ci = items.findByUserAndProduct(u, p).orElseThrow(() -> ApiException.notFound("Cart item"));
        checkStock(p, r.quantity());
        ci.setQuantity(r.quantity());
        return toResponse(items.findByUser(u));
    }

    public CartResponse remove(String email, Long productId) {
        AppUser u = user(email);
        items.findByUser(u).stream().filter(c -> c.getProduct().getId().equals(productId)).findFirst()
                .ifPresent(items::delete);
        return toResponse(items.findByUser(u));
    }

    public void clear(String email) {
        items.deleteAll(items.findByUser(user(email)));
    }
}
