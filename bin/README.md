# ShopWave: E-commerce REST API (Spring Boot 3 + Swagger)

Java 17, Spring Boot 3.3, Spring Data JPA (in-memory H2), Spring Security (HTTP Basic, BCrypt), Bean Validation, springdoc-openapi (Swagger UI).

## Run
Prerequisites: **JDK 17+** and **Maven 3.9+** (or just VS Code with the "Extension Pack for Java" + "Spring Boot Extension Pack", or IntelliJ, which bring Maven with them).

```bash
mvn spring-boot:run
```
In an IDE: open the folder as a Maven project and run `ShopApplication`.

Open **http://localhost:8080** (redirects to Swagger UI at `/swagger-ui.html`).

## Use it in Swagger
1. Click **Authorize** (top right) and enter a demo account:
   - Admin: `admin@shop.com` / `admin123`
   - Customer: `customer@shop.com` / `customer123`
2. Try the flow: `GET /api/products` -> `POST /api/cart/items` (`{"productId":1,"quantity":2}`) -> `POST /api/orders/checkout` (`{"shippingAddress":"1 Main St"}`) -> `GET /api/orders`.
3. Log in as admin to try `/api/admin/*` and product/category writes.

Public endpoints (no login): catalog, categories, `POST /api/auth/register`.

## Endpoints
| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `GET /api/auth/me` |
| Catalog | `GET /api/products` (q, categoryId, minPrice, maxPrice, page, size, sortBy, direction), `GET /api/products/{id}`; ADMIN: `POST/PUT/DELETE` |
| Categories | `GET /api/categories`; ADMIN: `POST/PUT/DELETE` |
| Cart | `GET /api/cart`, `POST/PUT /api/cart/items`, `DELETE /api/cart/items/{productId}`, `DELETE /api/cart` |
| Orders | `POST /api/orders/checkout`, `GET /api/orders`, `GET /api/orders/{id}`, `POST /api/orders/{id}/cancel` |
| Admin | `GET /api/admin/stats`, `GET /api/admin/orders?status=`, `PATCH /api/admin/orders/{id}/status` |

## Structure
```
config/      SecurityConfig, OpenApiConfig, DataSeeder, RootController
controller/  thin REST controllers (Swagger annotations)
service/     business rules, transactions
repo/        Spring Data repositories
model/       JPA entities
dto/         request/response records + Mapper
exception/   ApiException + GlobalExceptionHandler (uniform JSON errors)
```

## Business rules implemented
- Stock is checked when adding to cart and again at checkout; checkout is one transaction (order + stock decrement + cart cleared).
- `@Version` on `Product` (optimistic locking) prevents overselling on concurrent checkouts (HTTP 409).
- Order lines snapshot product name and price, so later price changes never alter past orders.
- Customers can cancel only their own PENDING orders; cancelling (customer or admin) restores stock. DELIVERED/CANCELLED orders are final.
- Product delete is a soft delete, so order history stays valid. Categories with products cannot be deleted.
- Public registration always creates CUSTOMER; admins are seeded.
- Users can only see their own orders and cart.

## Tests
`mvn test` runs `ShopFlowTest` (public catalog, 401 without login, 403 for customers on admin routes, add-to-cart + checkout).

## Production notes (not done in this prototype)
- H2 is in-memory and resets on restart. Switch to PostgreSQL/MySQL by changing the 4 `spring.datasource.*` lines, adding the driver, and using `ddl-auto=validate` with Flyway/Liquibase migrations.
- HTTP Basic is used for easy Swagger testing. Use JWT/OAuth2 over HTTPS in production; add rate limiting and CORS rules.
- No payment gateway, email, product images upload, reviews or wishlist yet; "PAID" status is set manually by an admin.
