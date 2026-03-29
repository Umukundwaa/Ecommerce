# 🛍️ E-Commerce API — Spring Boot

A complete e-commerce REST API built with Spring Boot, covering everything from the **She Can Code Week 10 assignment**:
JWT Authentication, Role-Based Access Control, OAuth2 Social Login, Pagination, Validation, Swagger UI, and more.

---

## 📁 Project Structure

```
src/main/java/com/ecommerce/
├── EcommerceApplication.java          ← Entry point
├── config/
│   ├── SecurityConfig.java            ← JWT filter chain, CORS, CSRF config
│   ├── OpenApiConfig.java             ← Swagger / OpenAPI setup
│   └── DataSeeder.java                ← Seeds users & products on startup
├── controller/
│   ├── AuthController.java            ← /api/auth/register, /login, /me
│   ├── ProductController.java         ← /api/products (CRUD + search + pagination)
│   ├── OrderController.java           ← /api/orders
│   └── UserController.java            ← /api/users (admin only)
├── dto/                               ← Request/Response objects (no model leaking)
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   ├── ProductRequest/Response.java
│   ├── OrderRequest/Response.java
│   ├── UserResponse.java
│   └── ApiResponse.java               ← Standard wrapper for all responses
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── GlobalExceptionHandler.java    ← Catches all exceptions, returns JSON
├── model/
│   ├── User.java
│   ├── Product.java
│   ├── Order.java
│   └── OrderItem.java
├── repository/
│   ├── UserRepository.java
│   ├── ProductRepository.java         ← Custom search + pagination queries
│   └── OrderRepository.java
├── security/
│   ├── CustomUserDetailsService.java  ← Loads user from DB for Spring Security
│   ├── JwtAuthenticationFilter.java   ← Reads JWT from header on every request
│   ├── CustomOAuth2UserService.java   ← Handles Google/GitHub login
│   └── OAuth2SuccessHandler.java      ← Issues our JWT after OAuth2 success
├── service/
│   ├── ProductService.java
│   ├── OrderService.java
│   └── UserService.java
└── util/
    └── JwtUtil.java                   ← Generate, parse, validate JWTs
```

---

## 🚀 How to Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Steps

```bash
# 1. Clone or extract this project
cd Ecommerce

# 2. Run the app (H2 in-memory DB — no setup needed!)
mvn spring-boot:run

# 3. Open Swagger UI to explore all endpoints
open http://localhost:8080/swagger-ui.html

# 4. Open H2 Console to see the database
open http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:ecommerce
# Username: sa | Password: password
```

### Default Test Accounts (seeded on startup)
| Username | Password | Role |
|----------|----------|------|
| `admin`  | `password` | ADMIN + USER |
| `user`   | `password` | USER |

---

## 🔐 API Endpoints

### Authentication (Public)
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/auth/register` | Create account, get JWT |
| POST | `/api/auth/login`    | Login, get JWT |
| GET  | `/api/auth/me`       | Get your profile (requires JWT) |

### Products (Public for GET, Admin for write)
| Method | URL | Auth |
|--------|-----|------|
| GET | `/api/products` | Public |
| GET | `/api/products/{id}` | Public |
| GET | `/api/products/search?keyword=shoes` | Public |
| GET | `/api/products/category/Electronics` | Public |
| POST | `/api/products` | Admin |
| PUT | `/api/products/{id}` | Admin |
| DELETE | `/api/products/{id}` | Admin |

### Orders (Authenticated users)
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/orders` | Place an order |
| GET  | `/api/orders/my` | My orders |
| GET  | `/api/orders/{id}` | Single order |
| GET  | `/api/orders` | All orders (Admin) |
| PATCH | `/api/orders/{id}/status` | Update status (Admin) |

### Users (Admin only)
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/users` | List all users |
| GET | `/api/users/{id}` | Get user |
| POST | `/api/users/{id}/promote` | Make user admin |
| DELETE | `/api/users/{id}` | Disable user |

---

## 🧪 Testing with Postman / curl

### 1. Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@test.com","password":"password123"}'
```

### 2. Login (get a token)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```
Copy the `token` from the response.

### 3. Use token in protected requests
```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 4. Place an order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingAddress": "123 Main St, Kigali",
    "items": [{"productId": 1, "quantity": 2}]
  }'
```

### 5. Admin: create a product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer ADMIN_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Smartphone",
    "description": "Latest model",
    "price": 599.99,
    "stockQuantity": 25,
    "category": "Electronics"
  }'
```

---

## 🌐 OAuth2 Social Login (Google)

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a project → Enable Google+ API
3. Create OAuth2 credentials → Web application
4. Set Authorized Redirect URI: `http://localhost:8080/login/oauth2/code/google`
5. Copy Client ID and Secret into `application.yml`
6. Visit `http://localhost:8080/oauth2/authorization/google` in your browser
7. Login with Google → you'll receive a JWT in the response

---

## 🔑 Key Concepts Covered

| Concept | Where |
|---------|-------|
| Security Filter Chain | `SecurityConfig.java` |
| JWT Generation & Validation | `JwtUtil.java` |
| JWT Filter (runs per request) | `JwtAuthenticationFilter.java` |
| UserDetailsService (load from DB) | `CustomUserDetailsService.java` |
| Role-Based Access (`@PreAuthorize`) | All controllers |
| OAuth2 Login | `CustomOAuth2UserService` + `OAuth2SuccessHandler` |
| Global Exception Handling | `GlobalExceptionHandler.java` |
| Input Validation (`@Valid`) | All request DTOs |
| Pagination & Sorting | `ProductController` + `ProductService` |
| DTO Pattern | `dto/` package |
| CORS config | `SecurityConfig.corsConfigurationSource()` |
| CSRF (disabled for stateless) | `SecurityConfig` — explained in comments |
| Swagger / OpenAPI | `OpenApiConfig.java` → `/swagger-ui.html` |
| Unit Tests | `EcommerceApplicationTests.java` |

---

## 🧪 Running Tests

```bash
mvn test
```

Tests cover:
- Public endpoints return 200 without auth
- Protected endpoints return 403 without auth
- `ROLE_USER` cannot delete products (403)
- `ROLE_ADMIN` can delete products (200)
- Register / login / validation errors
- Search and pagination

---

## ⚠️ Production Notes (from the assignment)

- Keep Client Secret out of version control — use environment variables
- Set short JWT expiry (15 minutes) in production
- Use a real database (PostgreSQL) instead of H2
- Use HTTPS in production
- Refresh tokens are not covered here but are needed in real apps
