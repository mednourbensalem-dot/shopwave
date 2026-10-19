package com.shopwave.repo;

import com.shopwave.model.AppUser;
import com.shopwave.model.CartItem;
import com.shopwave.model.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(AppUser user);
    Optional<CartItem> findByUserAndProduct(AppUser user, Product product);
}
