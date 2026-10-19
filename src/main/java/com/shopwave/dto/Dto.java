package com.shopwave.dto;

import com.shopwave.model.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** All request/response payloads in one place (Java records). */
public final class Dto {
    private Dto() {}

    public record RegisterRequest(@NotBlank String name, @NotBlank @Email String email,
                                  @NotBlank @Size(min = 6, max = 72) String password) {}
    public record UserResponse(Long id, String name, String email, String role) {}

    public record CategoryRequest(@NotBlank @Size(max = 60) String name) {}
    public record CategoryResponse(Long id, String name) {}

    public record ProductRequest(@NotBlank String name, String description,
                                 @NotNull @DecimalMin("0.01") BigDecimal price,
                                 @Min(0) int stock, Long categoryId, String imageUrl) {}
    public record ProductResponse(Long id, String name, String description, BigDecimal price,
                                  int stock, String category, String imageUrl) {}
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}

    public record CartItemRequest(@NotNull Long productId, @Min(1) int quantity) {}
    public record CartItemResponse(Long productId, String name, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {}
    public record CartResponse(List<CartItemResponse> items, BigDecimal total) {}

    public record CheckoutRequest(@NotBlank @Size(max = 250) String shippingAddress) {}
    public record OrderItemResponse(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {}
    public record OrderResponse(Long id, OrderStatus status, BigDecimal total, Instant createdAt,
                                String shippingAddress, String customer, List<OrderItemResponse> items) {}
    public record StatusRequest(@NotNull OrderStatus status) {}
    public record StatsResponse(long customers, long products, long orders, BigDecimal revenue) {}
}
