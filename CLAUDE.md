# Solex — AI Code Context

## What is this project
Solex is an online food ordering application (similar to GrabFood / ShopeeFood).
Backend: Spring Boot 3.5, Java 21, PostgreSQL.

## Required stack
- Java 21 — use records, sealed classes, text blocks where appropriate
- Spring Boot 3.5 (do not use 2.x or 3.2 APIs)
- Spring Data JPA + Hibernate 6
- PostgreSQL — schema managed by Hibernate ddl-auto=update
- Lombok — NEVER write boilerplate by hand
- Maven (not Gradle)

## Required conventions

### Naming
- Entity: PascalCase, singular (User, Order, MenuItem)
- Table: snake_case, plural (users, orders, menu_items)
- API endpoint: /api/v1/{resource} (kebab-case, plural)
- Branch: feature/SOL-{ticket}-short-description

### Package structure
com.solex.
  domain/      → Entities, Enums (NO business logic here)
  repository/  → Interfaces extending JpaRepository
  service/     → @Service, @Transactional, all business logic lives here
  controller/  → @RestController, validate input + delegate to service only
  dto/         → Request/Response (Java records or plain classes)
  mapper/      → MapStruct interfaces
  config/      → @Configuration classes
  exception/   → Custom exceptions + GlobalExceptionHandler
  event/       → Domain events (OrderCreatedEvent, etc.)
  util/        → Static helpers, JwtUtil

### Rules — never break these
- Controllers MUST NOT call Repositories directly
- Entities MUST NOT contain business logic (keep complex calculations in Service)
- NEVER use @Autowired field injection — constructor injection only
- NEVER return Entities from controllers — always map to DTOs first
- Every response must be wrapped in ApiResponse<T>
- NEVER throw raw Exception — use ResourceNotFoundException or BusinessException

## Standard patterns

### Creating a new feature (e.g. Review)
1. Create Entity in domain/
2. Create Repository in repository/
3. Create Request/Response DTOs (Java records preferred)
4. Implement Service with @Transactional
5. Implement Controller with @RestController

## Swagger / OpenAPI convention

We use SpringDoc OpenAPI 3. Already configured — do NOT add new dependencies.

### Rules
- Every controller class must have @Tag(name = "...", description = "...")
- Every endpoint must have @Operation(summary = "...")
- Only document HTTP 200 response — do NOT add @ApiResponse for error codes
- Use @Schema on DTO fields for documentation, not on Entity fields
- Response wrapper is always ApiResponse<T> — document the inner type only

### Example pattern (follow exactly)
@Tag(name = "Restaurants", description = "Restaurant management")
@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    @Operation(summary = "Get restaurant by ID")
    @GetMapping("/{id}")
    public ApiResponse<RestaurantResponse> getById(@PathVariable String id) { ... }
}

// DTO — document fields here, not on Entity
public record RestaurantResponse(
    @Schema(description = "Restaurant UUID") String id,
    @Schema(description = "Display name")   String name,
    @Schema(description = "Is accepting orders") boolean isOpen
) {}

## Stripe integration

### What's already done
- stripe-java SDK added to pom.xml (com.stripe:stripe-java:25.x)
- StripeConfig.java exists in config/ — reads secret key from application.yml
- application.yml already has: stripe.secret-key and stripe.webhook-secret

### What needs to be built (current task)
- PaymentService: createPaymentIntent(orderId, amount, currency)
- PaymentController: POST /api/v1/payments/stripe/intent
- PaymentController: POST /api/v1/payments/stripe/webhook (no JWT, whitelisted)
- StripeWebhookService: handle payment_intent.succeeded and payment_intent.failed

### Stripe-specific rules
- NEVER log full PaymentIntent object — log only id and status
- Webhook endpoint MUST verify Stripe-Signature header before processing
- On payment_intent.succeeded → call orderService.updateStatus(CONFIRMED)
- On payment_intent.failed → call orderService.updateStatus(CANCELLED)
- Webhook endpoint must be whitelisted in SecurityConfig (same pattern as VNPay)
- Always use stripe.webhook-secret from config for signature verification — never hardcode

### Error handling
- Resource not found → throw new ResourceNotFoundException("EntityName not found: " + id)
- Business rule violation → throw new BusinessException("Clear description of what went wrong")
- Validation failure → @Valid on @RequestBody, descriptive messages in annotations

## Domain model

User → has many Orders
Restaurant → has many MenuItems, has many Orders
Order → has many OrderItems, each OrderItem links to a MenuItem
OrderStatus: PENDING → CONFIRMED → PREPARING → READY → DELIVERING → DELIVERED
                                                               ↘ CANCELLED (only from PENDING or CONFIRMED)

## Current sprint
- Active sprint: Payment integration (VNPay)
- Active files: PaymentService, PaymentController, VNPayConfig
- Not yet implemented: Review/Rating, Recommendation engine

## Never do these
- NEVER add a new dependency without asking first
- NEVER change the package structure
- NEVER use var for simple primitives (int, short String)
- NEVER add obvious comments — only comment non-obvious or complex logic
- NEVER hardcode URLs, secrets, or config values — use application.yml + @Value