# Solex Backend

Solex is a food online shipping platform where customers order food, shippers (operators) deliver it, and the platform collects a **20% commission** on every transaction via Stripe Connect.

## How it works

- **Customer** — browses restaurants, places orders, and pays online (Stripe or VNPay)
- **Operator** — owns and manages the restaurant, and also handles delivery (shipper)
- **Platform** — sits in the middle; takes 20% of each order total as commission, settled automatically through Stripe Connect to the restaurant's connected account

## Tech stack

- Java 21 + Spring Boot 3.5
- PostgreSQL (schema managed by Hibernate `ddl-auto=update`)
- Spring Data JPA + Hibernate 6
- Spring Security + JWT
- Stripe Connect (payment + 20% commission split)
- VNPay (alternative payment gateway)
- Lombok, MapStruct, SpringDoc OpenAPI 3

## Architecture highlights

- **Strategy pattern** for payment gateways — adding a new gateway never touches existing code
- **Sealed interface `OrderState`** for order status transitions — invalid transitions throw at compile time
- **Single Responsibility** — services are split by responsibility (OrderCreationService, OrderStatusService, OrderQueryService, etc.)
- **ApiResponse\<T\>** wrapper on every endpoint
- Constructor injection only — no `@Autowired` field injection

## API

Base path: `/api/v1`

Swagger UI available at `/swagger-ui.html` when the app is running.

Key endpoints:

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Register customer or operator |
| POST | `/api/v1/auth/login` | Login, returns JWT |
| GET | `/api/v1/restaurants` | List restaurants |
| POST | `/api/v1/orders` | Place an order |
| POST | `/api/v1/payments` | Initiate payment (returns `clientSecret` for Stripe or `redirectUrl` for VNPay) |
| POST | `/api/v1/payments/stripe/webhook` | Stripe webhook (no JWT) |
| GET | `/api/v1/payments/vnpay/ipn` | VNPay IPN callback (no JWT) |

## Payment flow (Stripe)

1. Customer calls `POST /api/v1/payments` with `method: "STRIPE"`
2. Backend creates a `PaymentIntent` via Stripe Connect and returns `paymentIntentId` + `clientSecret`
3. Frontend calls `stripe.confirmCardPayment(clientSecret)` — no redirect needed
4. Stripe calls the webhook on success/failure → order status updated automatically
5. Platform commission (20%) is collected as `application_fee_amount` on the PaymentIntent

## Running locally

```bash
# 1. Set environment variables or edit application.yml
# Required: DB credentials, JWT secret, Stripe keys, VNPay keys

# 2. Run
./mvnw spring-boot:run
```

Requires Java 21 and a running PostgreSQL instance.
