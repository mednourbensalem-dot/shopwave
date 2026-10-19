package com.shopwave.config;

import com.shopwave.model.*;
import com.shopwave.repo.*;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Fictional demo data, loaded at startup when the database is empty. */
@Component
public class DataSeeder implements CommandLineRunner {
    private final AppUserRepository users;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final PasswordEncoder encoder;

    public DataSeeder(AppUserRepository users, CategoryRepository categories, ProductRepository products, PasswordEncoder encoder) {
        this.users = users;
        this.categories = categories;
        this.products = products;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;
        user("Admin", "admin@shop.com", "admin123", Role.ADMIN);
        user("Jane Customer", "customer@shop.com", "customer123", Role.CUSTOMER);

        Category el = cat("Electronics"), bk = cat("Books"), hm = cat("Home");
        product("Wireless Headphones", "Bluetooth over-ear, 30h battery", "79.90", 50, el);
        product("Mechanical Keyboard", "Hot-swappable, RGB backlight", "109.00", 30, el);
        product("USB-C Hub 7-in-1", "HDMI, SD card, 3x USB-A", "34.50", 100, el);
        product("Clean Code", "A handbook of agile software craftsmanship", "39.99", 25, bk);
        product("Designing Data-Intensive Applications", "Reliable, scalable systems", "49.90", 20, bk);
        product("Ceramic Coffee Mug", "350 ml, dishwasher safe", "12.00", 200, hm);
        product("LED Desk Lamp", "Dimmable, touch control", "27.80", 60, hm);
        product("Memory Foam Pillow", "Ergonomic, washable cover", "24.90", 40, hm);
    }

    private void user(String name, String email, String pass, Role role) {
        AppUser u = new AppUser();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(encoder.encode(pass));
        u.setRole(role);
        users.save(u);
    }

    private Category cat(String name) {
        Category c = new Category();
        c.setName(name);
        return categories.save(c);
    }

    private void product(String name, String desc, String price, int stock, Category c) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setCategory(c);
        products.save(p);
    }
}
