package com.shopwave.controller;

import com.shopwave.dto.Dto.OrderResponse;
import com.shopwave.dto.Dto.StatsResponse;
import com.shopwave.dto.Dto.StatusRequest;
import com.shopwave.model.OrderStatus;
import com.shopwave.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin")
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final OrderService orders;

    public AdminController(OrderService orders) { this.orders = orders; }

    @Operation(summary = "Dashboard numbers (ADMIN)")
    @GetMapping("/stats")
    public StatsResponse stats() { return orders.stats(); }

    @Operation(summary = "All orders, optional status filter (ADMIN)")
    @GetMapping("/orders")
    public List<OrderResponse> all(@RequestParam(required = false) OrderStatus status) { return orders.all(status); }

    @Operation(summary = "Change order status; CANCELLED restores stock (ADMIN)")
    @PatchMapping("/orders/{id}/status")
    public OrderResponse status(@PathVariable Long id, @Valid @RequestBody StatusRequest r) { return orders.updateStatus(id, r.status()); }
}
