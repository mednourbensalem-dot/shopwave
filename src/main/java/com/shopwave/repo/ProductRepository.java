package com.shopwave.repo;

import com.shopwave.model.Product;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByCategoryIdAndActiveTrue(Long categoryId);
    long countByActiveTrue();

    // cat = 0 means "any category"
    @Query("select p from Product p left join p.category c where p.active = true "
         + "and lower(p.name) like lower(concat('%', :q, '%')) "
         + "and (:cat = 0 or c.id = :cat) "
         + "and p.price >= :min and p.price <= :max")
    Page<Product> search(@Param("q") String q, @Param("cat") Long cat,
                         @Param("min") BigDecimal min, @Param("max") BigDecimal max, Pageable pageable);
}
