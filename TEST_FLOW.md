# Solex Backend — Complete Test Flow

Base URL: `http://localhost:8080`
All authenticated requests require: `Authorization: Bearer <token>`

---

## 1. Auth Flow

### 1.1 Check if email is available
```
POST /api/v1/auth/contact/check
{ "field": "EMAIL", "value": "user@test.com" }
```
**Expect:** 200 — `{ "exists": false }`

### 1.2 Send OTP to email
```
POST /api/v1/auth/otp/send
{ "field": "EMAIL", "value": "user@test.com" }
```
**Expect:** 200 — OTP sent to email

### 1.3 Verify OTP
```
POST /api/v1/auth/otp/verify
{ "field": "EMAIL", "value": "user@test.com", "otp": "123456" }
```
**Expect:** 200
**Error case:** wrong OTP → 400

### 1.4 Sign up (OTP must be verified first)
```
POST /api/v1/customer/sign-up
{
  "email": "user@test.com",
  "password": "pass123",
  "firstName": "Nguyen",
  "lastName": "Van A"
}
```
**Expect:** 201 — `{ token, user }` → save `token`
**Error case:** duplicate email → 409 Conflict

### 1.5 Sign in
```
POST /api/v1/auth/sign-in
{ "email": "user@test.com", "password": "pass123" }
```
**Expect:** 200 — `{ token, user }`
**Error case:** wrong password → 401

### 1.6 Logout
```
POST /api/v1/auth/logout   [Bearer token]
```
**Expect:** 200
**Verify:** old token no longer works → 401 on next request

---

## 2. Customer Profile Flow

### 2.1 Get profile
```
GET /api/v1/customer/profile   [Bearer token]
```
**Expect:** 200 — user info

### 2.2 Update profile
```
PUT /api/v1/customer/profile   [Bearer token]
{ "firstName": "Nguyen", "lastName": "Van B", "phone": "0901234567" }
```
**Expect:** 200

---

## 3. Address Flow

### 3.1 Create address (first address → auto-set as default)
```
POST /api/v1/customer/addresses   [Bearer token]
{
  "label": "Home",
  "firstName": "Nguyen",
  "lastName": "Van A",
  "phone": "0901234567",
  "street": "123 Nguyen Trai",
  "ward": "Ben Thanh",
  "district": "Quan 1",
  "province": "Ho Chi Minh"
}
```
**Expect:** 201 — address with `isDefault: true`

### 3.2 Create second address
```
POST /api/v1/customer/addresses   [Bearer token]
{ "label": "Office", ... }
```
**Expect:** 201 — `isDefault: false`

### 3.3 List addresses
```
GET /api/v1/customer/addresses   [Bearer token]
```
**Expect:** 200 — list of addresses

### 3.4 Set default address
```
PUT /api/v1/customer/addresses/{id}/default   [Bearer token]
```
**Expect:** 200 — that address becomes default, others become false

### 3.5 Delete address
```
DELETE /api/v1/customer/addresses/{id}   [Bearer token]
```
**Expect:** 200
**Error case:** delete another user's address → 400

---

## 4. Browse Restaurants & Menu

### 4.1 Get all open restaurants (public)
```
GET /api/v1/restaurants
```
**Expect:** 200 — list of open restaurants

### 4.2 Get restaurant by slug (public)
```
GET /api/v1/restaurants/{slug}
```
**Expect:** 200 — restaurant details with star counts
**Error case:** unknown slug → 404

### 4.3 Get restaurant menu (public)
```
GET /api/v1/restaurants/{slug}/menu
```
**Expect:** 200 — only products belonging to this restaurant with `isActive: true`
**Verify bug fix:** should NOT return products from other restaurants

### 4.4 Get product detail (public)
```
GET /api/v1/products/{id}
```
**Expect:** 200 — product with images and active variants

---

## 5. Cart Flow

### 5.1 View empty cart
```
GET /api/v1/cart   [Bearer token, CUSTOMER]
```
**Expect:** 200 — empty list

### 5.2 Add item to cart
```
POST /api/v1/cart/items   [Bearer token]
{ "productVariantId": 1, "quantity": 2 }
```
**Expect:** 200 — cart item with product info and total price
**Error case:** invalid variant ID → 404

### 5.3 Add same item again (upsert)
```
POST /api/v1/cart/items   [Bearer token]
{ "productVariantId": 1, "quantity": 1 }
```
**Expect:** 200 — quantity is now 3 (2+1)

### 5.4 Increment quantity
```
POST /api/v1/cart/items/{cartItemId}/update   [Bearer token]
{ "action": "+" }
```
**Expect:** 200 — quantity +1

### 5.5 Decrement quantity
```
POST /api/v1/cart/items/{cartItemId}/update   [Bearer token]
{ "action": "-" }
```
**Expect:** 200 — quantity -1
**Edge case:** quantity becomes 0 → item deleted, response data: null

### 5.6 Remove item directly
```
DELETE /api/v1/cart/items/{cartItemId}   [Bearer token]
```
**Expect:** 200
**Error case:** another user's cart item → 400

### 5.7 View cart (should reflect changes)
```
GET /api/v1/cart   [Bearer token]
```
**Expect:** 200 — updated cart

---

## 6. Order Flow (Customer)

### 6.1 Create order (cart must not be empty)
```
POST /api/v1/orders   [Bearer token]
{ "addressId": 1, "note": "Khong hanh" }
```
**Expect:** 201 — order with status PENDING, orderCode, items, totals
**Verify:** cart is cleared after order creation
**Error case:** empty cart → 400
**Error case:** address of another user → 400

### 6.2 Get my orders
```
GET /api/v1/orders   [Bearer token]
```
**Expect:** 200 — list of orders

### 6.3 Get order detail
```
GET /api/v1/orders/{id}   [Bearer token]
```
**Expect:** 200 — order with all items
**Error case:** another user's order → 400

---

## 7. Payment Flow

### 7.1 — Stripe

#### 7.1.1 Initiate Stripe payment
```
POST /api/v1/payments   [Bearer token]
{ "orderId": 1, "method": "STRIPE" }
```
**Expect:** 200 — `{ paymentId, transactionRef, clientSecret, redirectUrl: null, method, amount }`
→ Use `clientSecret` in Stripe.js on the frontend to complete payment

**Error case:** order not PENDING → 400
**Error case:** duplicate pending payment → 400

#### 7.1.2 Frontend confirms payment via Stripe.js
The frontend calls `stripe.confirmPayment({ clientSecret })`.
Stripe calls your webhook with `payment_intent.succeeded`.

#### 7.1.3 Stripe webhook fires (simulate with Stripe CLI)
```
stripe trigger payment_intent.succeeded
# or
POST /api/v1/payments/stripe/webhook
Headers: Stripe-Signature: <sig>
Body: <raw stripe event JSON>
```
**Expect:** 200 — `"OK"`
**Verify:** payment status → SUCCESS, order status → CONFIRMED

#### 7.1.4 Simulate payment failure
```
stripe trigger payment_intent.payment_failed
```
**Verify:** payment status → FAILED, order status → CANCELLED

#### 7.1.5 Get payment status
```
GET /api/v1/payments/{paymentId}   [Bearer token]
```
**Expect:** 200 — `{ status: "SUCCESS", paidAt: ... }`

---

### 7.2 — VNPay

#### 7.2.1 Initiate VNPay payment
```
POST /api/v1/payments   [Bearer token]
{ "orderId": 2, "method": "VNPAY" }
```
**Expect:** 200 — `{ paymentId, transactionRef, clientSecret: null, redirectUrl: "https://sandbox.vnpayment.vn/...", method, amount }`
→ Redirect browser to `redirectUrl`

#### 7.2.2 User pays on VNPay gateway
User is redirected back to `VNPAY_RETURN_URL?vnp_TxnRef=...&vnp_ResponseCode=00&...`

#### 7.2.3 VNPay return URL hit (browser)
```
GET /api/v1/payments/vnpay/return?vnp_TxnRef=xxx&vnp_ResponseCode=00&vnp_TransactionStatus=00&vnp_SecureHash=...
```
**Expect:** 200 — `{ status, orderCode, amount }`
**Error case:** tampered signature → 400

#### 7.2.4 VNPay IPN fires (server-to-server)
```
GET /api/v1/payments/vnpay/ipn?vnp_TxnRef=xxx&vnp_ResponseCode=00&vnp_TransactionStatus=00&vnp_SecureHash=...
```
**Expect:** 200 — `{ "RspCode": "00", "Message": "Confirm Success" }`
**Verify:** payment status → SUCCESS, order status → CONFIRMED

**Duplicate IPN:** same TxnRef sent again →
**Expect:** 200 — `{ "RspCode": "02", "Message": "Order already confirmed" }`

**Invalid signature:**
**Expect:** 200 — `{ "RspCode": "97", "Message": "Invalid signature" }`

---

## 8. Operator Flow

> Requires OPERATOR role token.

### 8.1 View order
```
GET /api/v1/operator/orders/{orderId}   [Bearer operator-token]
```
**Expect:** 200 — full order detail

### 8.2 Confirm order (PENDING → CONFIRMED)
```
PUT /api/v1/operator/orders/{orderId}/confirm   [Bearer operator-token]
```
**Expect:** 200
**Error case:** order already CONFIRMED → 400 (state machine)

### 8.3 Advance order
```
PUT /api/v1/operator/orders/{orderId}/advance   [Bearer operator-token]
```
Each call advances: CONFIRMED → PREPARING → READY → DELIVERING → DELIVERED

**Error case:** advance DELIVERED (terminal state) → 400

### 8.4 Cancel order
```
PUT /api/v1/operator/orders/{orderId}/cancel?reason=Out+of+stock   [Bearer operator-token]
```
**Expect:** 200 — order status → CANCELLED
**Error case:** cancel DELIVERED order → 400 (state machine prevents it)

---

## 9. Rating Flow

### 9.1 Create rating (CUSTOMER, must have ordered from this restaurant — not enforced yet)
```
POST /api/v1/restaurants/{restaurantId}/ratings   [Bearer token]
{ "rating": 5, "comment": "Ngon qua!" }
```
**Expect:** 201 — rating saved, restaurant star counters updated
**Error case:** rating < 1 or > 5 → 400

### 9.2 Update existing rating (same user, same restaurant → upsert)
```
POST /api/v1/restaurants/{restaurantId}/ratings   [Bearer token]
{ "rating": 4, "comment": "Van ngon" }
```
**Expect:** 200 — existing rating updated

### 9.3 Get ratings for restaurant (public)
```
GET /api/v1/restaurants/{restaurantId}/ratings
```
**Expect:** 200 — list of ratings, most recent first

---

## 10. Operator — Create Product

### 10.1 Create product (restaurant must exist, category must exist)
```
POST /api/v1/products   [Bearer operator-token]
{
  "name": "Pho Bo",
  "slug": "pho-bo",
  "description": "Pho truyen thong",
  "restaurantId": 1,
  "categoryId": 1,
  "basePrice": 50000,
  "isActive": true,
  "images": ["https://cdn.example.com/pho.jpg"]
}
```
**Expect:** 201 — product with images list
**Error case:** missing restaurantId → 400
**Error case:** unknown restaurantId → 404

---

## 11. DB Migration Notes

After adding `restaurant_id` to `products` table:

If existing rows in `products` table → Hibernate will fail to add NOT NULL column.
Run this first:
```sql
-- Option A: truncate existing test data
TRUNCATE TABLE order_items, orders, cart_items, products CASCADE;

-- Option B: add column manually then let Hibernate handle the NOT NULL constraint
ALTER TABLE products ADD COLUMN restaurant_id BIGINT;
UPDATE products SET restaurant_id = 1;  -- assign to a known restaurant
ALTER TABLE products ALTER COLUMN restaurant_id SET NOT NULL;
ALTER TABLE products ADD CONSTRAINT fk_product_restaurant
  FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
```

---

## 12. Environment Variables Required

```
DATABASE_URL=jdbc:postgresql://localhost:5432/solex
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
JWT_SECRET=your-256-bit-secret-here
RESEND_API_KEY=re_xxxxx
ESMS_API_KEY=xxxxx
ESMS_SECRET=xxxxx
STRIPE_SECRET_KEY=sk_test_xxxxx
STRIPE_WEBHOOK_SECRET=whsec_xxxxx
VNPAY_TMN_CODE=XXXXXXXX
VNPAY_HASH_SECRET=your-vnpay-hash-secret
VNPAY_RETURN_URL=http://localhost:8080/api/v1/payments/vnpay/return
VNPAY_IPN_URL=http://your-public-url/api/v1/payments/vnpay/ipn
```
