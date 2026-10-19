package com.shopwave.service;

import com.shopwave.dto.Dto.CheckoutRequest;
import com.shopwave.dto.Dto.OrderResponse;
import com.shopwave.dto.Dto.StatsResponse;
import com.shopwave.dto.Mapper;
import com.shopwave.exception.ApiException;
import com.shopwave.model.*;
import com.shopwave.repo.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orders;
    private final CartItemRepository cartItems;
    private final AppUserRepository users;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, CartItemRepository cartItems, AppUserRepository users, ProductRepository products) {
        this.orders = orders;
        this.cartItems = cartItems;
        this.users = users;
        this.products = products;
    }

    private AppUser user(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }

    /** Converts the cart into an order, decrements stock, empties the cart (one transaction). */
    public OrderResponse checkout(String email, CheckoutRequest req) {
        AppUser u = user(email);
        List<CartItem> cart = cartItems.findByUser(u);
        if (cart.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty");

        Order o = new Order();
        o.setUser(u);
        o.setShippingAddress(req.shippingAddress().trim());
        o.setStatus(OrderStatus.PENDING);
        o.setCreatedAt(Instant.now());
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart) {
            Product p = ci.getProduct();
            if (!p.isActive() || p.getStock() < ci.getQuantity())
                throw new ApiException(HttpStatus.CONFLICT, "Insufficient stock for " + p.getName());
            p.setStock(p.getStock() - ci.getQuantity());
            OrderItem oi = new OrderItem();
            oi.setOrder(o);
            oi.setProduct(p);
            oi.setProductName(p.getName());
            oi.setUnitPrice(p.getPrice());
            oi.setQuantity(ci.getQuantity());
            o.getItems().add(oi);
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        o.setTotal(total);
        orders.save(o);
        cartItems.deleteAll(cart);
        return Mapper.order(o);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> mine(String email) {
        return orders.findByUserOrderByCreatedAtDesc(user(email)).stream().map(Mapper::order).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse mine(String email, Long id) {
        return Mapper.order(owned(email, id));
    }

    /** Customers may cancel their own order only while it is still PENDING. */
    public OrderResponse cancel(String email, Long id) {
        Order o = owned(email, id);
        if (o.getStatus() != OrderStatus.PENDING)
            throw new ApiException(HttpStatus.CONFLICT, "Only PENDING orders can be cancelled (status: " + o.getStatus() + ")");
        restock(o);
        o.setStatus(OrderStatus.CANCELLED);
        return Mapper.order(o);
    }

    // ---- admin ----
    @Transactional(readOnly = true)
    public List<OrderResponse> all(OrderStatus status) {
        return orders.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(o -> status == null || o.getStatus() == status).map(Mapper::order).toList();
    }

    public OrderResponse updateStatus(Long id, OrderStatus next) {
        Order o = orders.findById(id).orElseThrow(() -> ApiException.notFound("Order"));
        if (o.getStatus() == OrderStatus.CANCELLED || o.getStatus() == OrderStatus.DELIVERED)
            throw new ApiException(HttpStatus.CONFLICT, "Order is " + o.getStatus() + " and can no longer change");
        if (next == OrderStatus.CANCELLED) restock(o);
        o.setStatus(next);
        return Mapper.order(o);
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        BigDecimal revenue = orders.findAll().stream().filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new StatsResponse(users.countByRole(Role.CUSTOMER), products.countByActiveTrue(), orders.count(), revenue);
    }

    private Order owned(String email, Long id) {
        AppUser u = user(email);
        return orders.findById(id).filter(o -> o.getUser().getId().equals(u.getId()))
                .orElseThrow(() -> ApiException.notFound("Order"));
    }

    private void restock(Order o) {
        for (OrderItem i : o.getItems()) i.getProduct().setStock(i.getProduct().getStock() + i.getQuantity());
    }
}
