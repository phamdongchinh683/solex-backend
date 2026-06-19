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
  domain/      → Entities, Enums, state/ (OrderState sealed interface)
  repository/  → Interfaces extending JpaRepository + QueryRepositories
  service/     → @Service, @Transactional, all business logic lives here
                 Organized by sub-packages: order/, payment/, notification/
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

## OOP + SOLID conventions

### S — Single Responsibility
- Never create a god-service. Split services by responsibility inside sub-packages:
  - service/order/OrderCreationService.java   → handles order creation only
  - service/order/OrderStatusService.java     → handles status transitions only
  - service/order/OrderQueryService.java      → handles read/query only
- Complex queries must live in a dedicated QueryRepository (e.g. OrderQueryRepository),
  not mixed into JpaRepository interfaces.

### O — Open/Closed (Strategy pattern for Payment)
- Each payment gateway is a separate @Component implementing PaymentStrategy interface.
- PaymentService resolves the correct strategy at runtime — never use if/else on PaymentMethod.
- Adding a new gateway (e.g. MoMo) means adding a new Strategy class only — never modify PaymentService.

```java
// Interface lives in service/payment/
public interface PaymentStrategy {
    PaymentResult process(PaymentRequest request);
    boolean supports(PaymentMethod method);
}

// PaymentService resolves strategy — never modified when adding new gateways
@Service
public class PaymentService {
    private final List<PaymentStrategy> strategies;

    public PaymentResult process(PaymentRequest request) {
        return strategies.stream()
            .filter(s -> s.supports(request.method()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(
                "Unsupported payment method: " + request.method()))
            .process(request);
    }
}
```

### L — Liskov / State pattern for OrderStatus
- Order status transitions are enforced via a sealed interface in domain/state/.
- Each state implements confirm(), cancel(), nextStep() — invalid transitions throw BusinessException.
- OrderStatusService calls state methods only — no if/else status checks scattered in services.

```java
// domain/state/OrderState.java
public sealed interface OrderState
    permits PendingState, ConfirmedState, PreparingState,
            ReadyState, DeliveringState, DeliveredState, CancelledState {
    OrderStatus status();
    OrderState confirm();
    OrderState cancel();
    OrderState nextStep();
}
```

### I — Interface Segregation
- JpaRepository interfaces stay thin — CRUD only.
- Complex queries (joins, aggregations, pagination with filters) go into a separate
  @Repository class using EntityManager or @Query, named *QueryRepository.
- Never add dozens of findBy* methods to a single JpaRepository.

### D — Dependency Inversion
- External integrations (push notifications, email, SMS) are hidden behind a Port interface
  in the service layer. Concrete adapters implement the interface.
- Services depend on the Port interface — never on the concrete adapter directly.

```java
// service/notification/NotificationPort.java
public interface NotificationPort {
    void sendOrderConfirmed(Order order);
    void sendOrderCancelled(Order order);
}

// Concrete adapter in infrastructure/ (future)
@Component
public class FirebaseNotificationAdapter implements NotificationPort { ... }
```

## Unique constraint handling

### 3-layer protection for email/phone uniqueness
1. @Valid on @RequestBody — rejects wrong format before touching the DB (@Email, @NotBlank)
2. existsByEmail() / existsByPhone() in Service before save — throws BusinessException with clear message
3. DB @UniqueConstraint as final safety net for race conditions — GlobalExceptionHandler
   catches DataIntegrityViolationException, parses constraint name, returns 409 Conflict

### Constraint naming convention (required for handler parsing)
```java
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_users_phone", columnNames = "phone")
    }
)
```

### GlobalExceptionHandler must handle
- DataIntegrityViolationException → 409 Conflict (parse constraint name → Vietnamese message)
- BusinessException              → 400 Bad Request
- ResourceNotFoundException      → 404 Not Found
- MethodArgumentNotValidException → 400 Bad Request with field-level errors

## Standard patterns

### Creating a new feature (e.g. Review)
1. Create Entity in domain/
2. Create Repository in repository/
3. Create Request/Response DTOs (Java records preferred)
4. Implement Service with @Transactional — split by responsibility if complex
5. Implement Controller with @RestController

### Adding a new payment gateway
1. Create GatewayNameStrategy.java in service/payment/ implementing PaymentStrategy
2. Annotate with @Component — Spring auto-discovers and injects into PaymentService
3. Whitelist webhook endpoint in SecurityConfig
4. Add gateway config to application.yml + @Value in config class
5. NEVER modify PaymentService or existing strategies

## Swagger / OpenAPI convention

We use SpringDoc OpenAPI 3. Already configured — do NOT add new dependencies.

### Rules
- Every controller class must have @Tag(name = "...", description = "...")
- Every endpoint must have @Operation(summary = "...")
- Only document HTTP 200 response — do NOT add @ApiResponse for error codes
- Use @Schema on DTO fields for documentation, not on Entity fields
- Response wrapper is always ApiResponse<T> — document the inner type only

### Example pattern (follow exactly)
```java
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
    @Schema(description = "Restaurant UUID")        String id,
    @Schema(description = "Display name")           String name,
    @Schema(description = "Is accepting orders")    boolean isOpen
) {}
```

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
- On payment_intent.failed    → call orderService.updateStatus(CANCELLED)
- Webhook endpoint must be whitelisted in SecurityConfig (same pattern as VNPay)
- Always use stripe.webhook-secret from config for signature verification — never hardcode

### Error handling
- Resource not found      → throw new ResourceNotFoundException("EntityName not found: " + id)
- Business rule violation → throw new BusinessException("Clear description of what went wrong")
- Validation failure      → @Valid on @RequestBody, descriptive messages in annotations

## Domain model

User → has many Orders
Restaurant → has many MenuItems, has many Orders
Order → has many OrderItems, each OrderItem links to a MenuItem
OrderStatus: PENDING → CONFIRMED → PREPARING → READY → DELIVERING → DELIVERED
                                                              ↘ CANCELLED (only from PENDING or CONFIRMED)

## Current sprint
- Active sprint: Payment integration (VNPay + Stripe)
- Active files: PaymentService, PaymentController, VNPayConfig, StripeConfig
- In progress: PaymentStrategy pattern refactor
- Not yet implemented: Review/Rating, Recommendation engine, Notification service

## Never do these
- NEVER add a new dependency without asking first
- NEVER change the package structure
- NEVER use var for simple primitives (int, String)
- NEVER hardcode URLs, secrets, or config values — use application.yml + @Value
- NEVER create a god-service — split by responsibility
- NEVER use if/else on PaymentMethod enum — use Strategy pattern
- NEVER scatter status transition checks — use OrderState sealed interface
- NEVER NOT SHOW FIELDS NOT NESSECARY COLUMN
- NEVER COMMENT CODE