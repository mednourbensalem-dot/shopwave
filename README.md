# 🛒 ShopWave

**A full-featured e-commerce REST API with a built-in storefront, built on Spring Boot 3 and documented with Swagger.**

![Java](https://img.shields.io/badge/Java-25_LTS-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-HTTP_Basic-6DB33F?logo=springsecurity&logoColor=white)
![H2](https://img.shields.io/badge/Database-H2_in--memory-blue)
![OpenAPI](https://img.shields.io/badge/OpenAPI-Swagger_UI-85EA2D?logo=swagger&logoColor=black)

ShopWave covers the whole shopping flow: browse and search products, register and sign in, keep a persistent server-side cart, check out, and review order history. Admins get management endpoints for products, categories, orders, and store stats.

---

## 📑 Table of Contents

- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [Quick Start](#-quick-start)
- [Demo Accounts](#-demo-accounts)
- [Try It in Swagger](#-try-it-in-swagger)
- [API Reference](#-api-reference)
- [Project Structure](#-project-structure)
- [Business Rules](#-business-rules)
- [Testing](#-testing)
- [Production Notes](#-production-notes)

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 (LTS) |
| Framework | Spring Boot 3.5 |
| Persistence | Spring Data JPA + H2 (in-memory) |
| Security | Spring Security, HTTP Basic, BCrypt password hashing |
| Validation | Bean Validation |
| API Docs | springdoc-openapi (Swagger UI) |
| Build | Maven (Maven Wrapper included) |

---

## ✨ Features

- **Storefront:** public product browsing, search, category filters, and sorting
- **Accounts:** customer registration and sign-in
- **Cart:** persistent, server-side cart tied to the user
- **Checkout & orders:** place orders, view history, cancel pending orders
- **Admin tools:** manage products, categories, and order statuses, plus dashboard stats
- **Interactive docs:** try every endpoint from Swagger UI
- **Uniform errors:** consistent JSON error responses across the API

> Checkout creates an order only. No payment gateway is configured.

---

## 🚀 Quick Start

### Prerequisites

- **JDK 25+**

No separate Maven installation is needed. The Maven Wrapper downloads the right version the first time you run it.

### Run

**Windows (PowerShell):**

```powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**

```bash
./mvnw spring-boot:run
```

Or open the folder as a Maven project in your IDE and run `ShopApplication`.

### Open the app

| What | URL |
|---|---|
| 🛍️ Storefront | http://localhost:8080 |
| 📖 Swagger UI (API docs) | http://localhost:8080/swagger-ui.html |

---

## 🔑 Demo Accounts

| Role | Email | Password |
|---|---|---|
| Customer | `customer@shop.com` | `customer123` |
| Admin | `admin@shop.com` | `admin123` |

Sign in as the customer to try the shopping flow in the storefront.

---

## 🧪 Try It in Swagger

1. Open http://localhost:8080/swagger-ui.html
2. Click **Authorize** (top right) and enter a demo account.
3. Run through the shopping flow:

   | Step | Request | Body |
   |---|---|---|
   | 1. Browse | `GET /api/products` | n/a |
   | 2. Add to cart | `POST /api/cart/items` | `{"productId":1,"quantity":2}` |
   | 3. Check out | `POST /api/orders/checkout` | `{"shippingAddress":"1 Main St"}` |
   | 4. View orders | `GET /api/orders` | n/a |

4. Authorize as **admin** to try `/api/admin/*` and product/category write operations.

**Public endpoints (no login required):** product catalog, categories, and `POST /api/auth/register`.

### Or with curl

```bash
# Browse the catalog (public)
curl http://localhost:8080/api/products

# Add to cart as the demo customer
curl -u customer@shop.com:customer123 \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}' \
  http://localhost:8080/api/cart/items

# Checkout
curl -u customer@shop.com:customer123 \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress":"1 Main St"}' \
  http://localhost:8080/api/orders/checkout
```

---

## 📡 API Reference

| Area | Endpoints | Access |
|---|---|---|
| **Auth** | `POST /api/auth/register`<br>`GET /api/auth/me` | Register is public; `me` requires login |
| **Catalog** | `GET /api/products` (`q`, `categoryId`, `minPrice`, `maxPrice`, `page`, `size`, `sortBy`, `direction`)<br>`GET /api/products/{id}`<br>`POST` / `PUT` / `DELETE /api/products` | Reads are public; writes are **ADMIN** |
| **Categories** | `GET /api/categories`<br>`POST` / `PUT` / `DELETE /api/categories` | Reads are public; writes are **ADMIN** |
| **Cart** | `GET /api/cart`<br>`POST` / `PUT /api/cart/items`<br>`DELETE /api/cart/items/{productId}`<br>`DELETE /api/cart` | Authenticated |
| **Orders** | `POST /api/orders/checkout`<br>`GET /api/orders`<br>`GET /api/orders/{id}`<br>`POST /api/orders/{id}/cancel` | Authenticated (own orders only) |
| **Admin** | `GET /api/admin/stats`<br>`GET /api/admin/orders?status=`<br>`PATCH /api/admin/orders/{id}/status` | **ADMIN** |

Full request/response schemas are available in Swagger UI.

---

## 🗂️ Project Structure

```
config/      SecurityConfig, OpenApiConfig, DataSeeder, RootController
controller/  Thin REST controllers (Swagger annotations)
service/     Business rules and transactions
repo/        Spring Data repositories
model/       JPA entities
dto/         Request/response records + Mapper
exception/   ApiException + GlobalExceptionHandler (uniform JSON errors)
```

---

## 📐 Business Rules

**Inventory & checkout**
- Stock is checked when adding to cart and again at checkout.
- Checkout is a single transaction: order created, stock decremented, cart cleared.
- `@Version` on `Product` (optimistic locking) prevents overselling on concurrent checkouts and returns **HTTP 409**.

**Orders**
- Order lines snapshot the product name and price, so later price changes never alter past orders.
- Customers can cancel only their own `PENDING` orders.
- Cancelling (by customer or admin) restores stock.
- `DELIVERED` and `CANCELLED` orders are final.

**Catalog**
- Product deletion is a **soft delete**, so order history stays valid.
- Categories that still contain products cannot be deleted.

**Users & security**
- Public registration always creates a `CUSTOMER`; admins are seeded.
- Users can only see their own orders and cart.

---

## ✅ Testing

**Windows (PowerShell):**

```powershell
.\mvnw.cmd test
```

**macOS / Linux:**

```bash
./mvnw test
```

`ShopFlowTest` covers:
- Public catalog access
- `401` when not logged in
- `403` for customers on admin routes
- Add to cart and checkout

---

## ⚠️ Production Notes

This is a prototype. Before going live, consider the following.

**Database**
- H2 is in-memory and **resets on every restart**.
- To move to PostgreSQL or MySQL: change the four `spring.datasource.*` properties, add the JDBC driver, and set `ddl-auto=validate` with Flyway or Liquibase migrations.

**Security**
- HTTP Basic is used for easy Swagger testing. Use **JWT or OAuth2 over HTTPS** in production, and add rate limiting and CORS rules.

**Not yet implemented**
- Payment gateway (the `PAID` status is set manually by an admin)
- Email notifications
- Product image uploads
- Reviews and wishlists
