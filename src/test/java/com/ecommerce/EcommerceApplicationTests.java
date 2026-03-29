package com.ecommerce;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for secured endpoints.
 *
 * @SpringBootTest  — loads the full application context
 * @AutoConfigureMockMvc — sets up MockMvc without a real server
 * @DirtiesContext — resets DB between tests
 *
 * Key annotations:
 *   @WithMockUser           — simulates a logged-in user with ROLE_USER
 *   @WithMockUser(roles="ADMIN") — simulates an admin user
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EcommerceApplicationTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        // Create a test product we can use across tests
        savedProduct = productRepository.save(Product.builder()
                .name("Test Laptop")
                .description("A test laptop")
                .price(new BigDecimal("999.99"))
                .stockQuantity(10)
                .category("Electronics")
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. PUBLIC ENDPOINTS — no auth required
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/products — public, returns 200 without login")
    void getProducts_noAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/products/{id} — public, returns product")
    void getProductById_noAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/products/" + savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Laptop"));
    }

    @Test
    @DisplayName("GET /api/products/{id} — non-existent product returns 404")
    void getProductById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/products/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. AUTHENTICATION — register & login
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("POST /api/auth/register — creates user and returns JWT")
    void register_validRequest_returnsJwt() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("token");
    }

    @Test
    @DisplayName("POST /api/auth/register — duplicate username returns 400")
    void register_duplicateUsername_returns400() throws Exception {
        // First registration
        RegisterRequest req = new RegisterRequest();
        req.setUsername("dupuser");
        req.setEmail("first@test.com");
        req.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Second registration with same username
        req.setEmail("second@test.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/register — missing fields returns 400 with validation errors")
    void register_missingFields_returns400() throws Exception {
        RegisterRequest empty = new RegisterRequest(); // all fields null

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empty)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.username").exists())
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login — valid credentials return JWT")
    void login_validCredentials_returnsJwt() throws Exception {
        // Register first
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("logintest");
        reg.setEmail("logintest@test.com");
        reg.setPassword("mypassword");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        // Then login
        LoginRequest login = new LoginRequest();
        login.setUsername("logintest");
        login.setPassword("mypassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value("logintest"));
    }

    @Test
    @DisplayName("POST /api/auth/login — wrong password returns 401")
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername("admin");
        login.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. PROTECTED ENDPOINTS — requires authentication
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/auth/me — without token returns 403")
    void getMe_noAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users — without auth returns 403")
    void getUsers_noAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 4. ROLE-BASED ACCESS CONTROL
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/products/{id} — ROLE_USER gets 403 Forbidden")
    void deleteProduct_asUser_returns403() throws Exception {
        mockMvc.perform(delete("/api/products/" + savedProduct.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/products/{id} — ROLE_ADMIN succeeds with 200")
    void deleteProduct_asAdmin_returns200() throws Exception {
        mockMvc.perform(delete("/api/products/" + savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/products — ROLE_USER gets 403 Forbidden")
    void createProduct_asUser_returns403() throws Exception {
        String body = """
                {
                  "name": "New Phone",
                  "price": 299.99,
                  "stockQuantity": 20
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/products — ROLE_ADMIN can create a product")
    void createProduct_asAdmin_returns201() throws Exception {
        String body = """
                {
                  "name": "New Phone",
                  "description": "A brand new phone",
                  "price": 299.99,
                  "stockQuantity": 20,
                  "category": "Electronics"
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("New Phone"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/users — ROLE_USER gets 403 (admin-only endpoint)")
    void getUsers_asUser_returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/users — ROLE_ADMIN gets 200")
    void getUsers_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 5. SEARCH AND PAGINATION
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /api/products/search?keyword=laptop — public search works")
    void searchProducts_returnsResults() throws Exception {
        mockMvc.perform(get("/api/products/search").param("keyword", "laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /api/products?page=0&size=5 — pagination works")
    void getProducts_withPagination_returns200() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "price")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageable").exists());
    }
}
