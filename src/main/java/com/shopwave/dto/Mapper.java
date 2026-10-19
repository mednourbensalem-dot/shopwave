package com.shopwave.dto;

import com.shopwave.dto.Dto.*;
import com.shopwave.model.*;
import java.math.BigDecimal;

public final class Mapper {
    private Mapper() {}

    public static UserResponse user(AppUser u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name());
    }

    public static ProductResponse product(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock(),
                p.getCategory() == null ? null : p.getCategory().getName(), p.getImageUrl());
    }

    public static CartItemResponse cartItem(CartItem c) {
        Product p = c.getProduct();
        return new CartItemResponse(p.getId(), p.getName(), p.getPrice(), c.getQuantity(),
                p.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())));
    }

    public static OrderResponse order(Order o) {
        return new OrderResponse(o.getId(), o.getStatus(), o.getTotal(), o.getCreatedAt(), o.getShippingAddress(),
                o.getUser().getEmail(),
                o.getItems().stream().map(i -> new OrderItemResponse(i.getProduct().getId(), i.getProductName(),
                        i.getUnitPrice(), i.getQuantity(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))).toList());
    }
}
