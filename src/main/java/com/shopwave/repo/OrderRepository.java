package com.shopwave.repo;

import com.shopwave.model.AppUser;
import com.shopwave.model.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(AppUser user);
}
