package com.shopwave.controller;

import com.shopwave.dto.Dto.CartItemRequest;
import com.shopwave.dto.Dto.CartResponse;
import com.shopwave.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart")
@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cart;

    public CartController(CartService cart) { this.cart = cart; }

    @Operation(summary = "View my cart")
    @GetMapping
    public CartResponse get(Principal p) { return cart.get(p.getName()); }

    @Operation(summary = "Add a product (quantity is added to any existing line; stock is checked)")
    @PostMapping("/items")
    public CartResponse add(Principal p, @Valid @RequestBody CartItemRequest r) { return cart.add(p.getName(), r); }

    @Operation(summary = "Set the exact quantity of a cart line")
    @PutMapping("/items")
    public CartResponse update(Principal p, @Valid @RequestBody CartItemRequest r) { return cart.update(p.getName(), r); }

    @Operation(summary = "Remove a product from my cart")
    @DeleteMapping("/items/{productId}")
    public CartResponse remove(Principal p, @PathVariable Long productId) { return cart.remove(p.getName(), productId); }

    @Operation(summary = "Empty my cart")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(Principal p) { cart.clear(p.getName()); }
}
