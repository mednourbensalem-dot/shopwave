package com.shopwave.controller;

import com.shopwave.dto.Dto.CheckoutRequest;
import com.shopwave.dto.Dto.OrderResponse;
import com.shopwave.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Orders")
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orders;

    public OrderController(OrderService orders) { this.orders = orders; }

    @Operation(summary = "Checkout: turn my cart into an order and decrement stock")
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(Principal p, @Valid @RequestBody CheckoutRequest r) { return orders.checkout(p.getName(), r); }

    @Operation(summary = "My order history")
    @GetMapping
    public List<OrderResponse> mine(Principal p) { return orders.mine(p.getName()); }

    @Operation(summary = "One of my orders")
    @GetMapping("/{id}")
    public OrderResponse one(Principal p, @PathVariable Long id) { return orders.mine(p.getName(), id); }

    @Operation(summary = "Cancel my order (only while PENDING; stock is restored)")
    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(Principal p, @PathVariable Long id) { return orders.cancel(p.getName(), id); }
}
