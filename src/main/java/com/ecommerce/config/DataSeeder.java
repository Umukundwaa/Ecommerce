package com.ecommerce.config;

import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedProducts();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        User admin = User.builder()
                .username("admin")
                .email("admin@ecommerce.com")
                .password(passwordEncoder.encode("password"))
                .firstName("Admin")
                .lastName("User")
                .roles(Set.of("ROLE_ADMIN", "ROLE_USER"))
                .provider("local")
                .build();

        User user = User.builder()
                .username("user")
                .email("user@ecommerce.com")
                .password(passwordEncoder.encode("password"))
                .firstName("Regular")
                .lastName("User")
                .roles(Set.of("ROLE_USER"))
                .provider("local")
                .build();

        userRepository.saveAll(List.of(admin, user));
        log.info("✅ Seeded 2 users: admin / user (password: 'password')");
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;

        List<Product> products = List.of(
                Product.builder()
                        .name("Wireless Headphones")
                        .description("High-quality Bluetooth headphones with noise cancellation")
                        .price(new BigDecimal("79.99"))
                        .stockQuantity(50)
                        .category("Electronics")
                        .imageUrl("https://example.com/headphones.jpg")
                        .build(),
                Product.builder()
                        .name("Running Shoes")
                        .description("Lightweight running shoes for all terrains")
                        .price(new BigDecimal("59.99"))
                        .stockQuantity(100)
                        .category("Footwear")
                        .imageUrl("https://example.com/shoes.jpg")
                        .build(),
                Product.builder()
                        .name("Coffee Maker")
                        .description("12-cup programmable coffee maker with thermal carafe")
                        .price(new BigDecimal("49.99"))
                        .stockQuantity(30)
                        .category("Kitchen")
                        .imageUrl("https://example.com/coffee.jpg")
                        .build(),
                Product.builder()
                        .name("Yoga Mat")
                        .description("Non-slip eco-friendly yoga mat, 6mm thick")
                        .price(new BigDecimal("29.99"))
                        .stockQuantity(75)
                        .category("Sports")
                        .imageUrl("https://example.com/yogamat.jpg")
                        .build(),
                Product.builder()
                        .name("Desk Lamp")
                        .description("LED desk lamp with adjustable brightness and USB charging port")
                        .price(new BigDecimal("34.99"))
                        .stockQuantity(60)
                        .category("Electronics")
                        .imageUrl("https://example.com/lamp.jpg")
                        .build()
        );

        productRepository.saveAll(products);
        log.info("✅ Seeded {} products", products.size());
    }
}
