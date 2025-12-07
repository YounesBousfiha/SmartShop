# SmartShop Application - Complete Developer Guide

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Domain Layer](#domain-layer)
4. [Application Layer](#application-layer)
5. [Presentation Layer](#presentation-layer)
6. [Infrastructure Layer](#infrastructure-layer)
7. [Testing](#testing)
8. [Business Rules](#business-rules)

---

## Overview

**SmartShop** is a Spring Boot e-commerce application built using Domain-Driven Design (DDD) and Clean Architecture principles. It manages clients, products, orders, and payments with role-based access control.

### Tech Stack
- **Framework**: Spring Boot 3.x
- **ORM**: JPA/Hibernate
- **Database**: PostgreSQL (configurable)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Architecture**: Layered (Domain, Application, Presentation, Infrastructure)

---

## Architecture

The application follows a **4-layer architecture**:

```
┌─────────────────────────────────┐
│   Presentation Layer            │  ← REST Controllers, DTOs, Exception Handlers
├─────────────────────────────────┤
│   Application Layer             │  ← Services, Mappers, Orchestration
├─────────────────────────────────┤
│   Domain Layer                  │  ← Entities, Value Objects, Business Logic
├─────────────────────────────────┤
│   Infrastructure Layer          │  ← Repositories, Configuration, Utils
└─────────────────────────────────┘
```

---

## Domain Layer

The domain layer contains the core business entities and logic, independent of frameworks.

### Entities

#### 1. **User** (Abstract Base Class)
**Location**: `com.jartiste.smartshop.domain.entity.User`

**Purpose**: Base entity for all system users using Single Table Inheritance.

**Attributes**:
- `id`: Unique identifier
- `username`: Login username
- `password`: Hashed password
- `role`: User role (ADMIN or CLIENT)
- `createdAt`, `updatedAt`: Audit timestamps

**Inheritance Strategy**: JOINED table inheritance with discriminator column

---

#### 2. **Client** (extends User)
**Location**: `com.jartiste.smartshop.domain.entity.Client`

**Purpose**: Represents customer accounts with loyalty tier system.

**Attributes**:
- `nom`: Client name
- `tier`: Customer loyalty tier (BRONZE, SILVER, GOLD, PLATINUM)
- `totalOrders`: Number of orders placed
- `totalSpent`: Cumulative spending amount
- `firstOrderDate`: Date of first order
- `lastOrderDate`: Date of most recent order

**Methods**:

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| `updateStats()` | `BigDecimal orderAmount` | void | Updates total orders and spent, recalculates tier |
| `recalculateTier()` | - | void | Determines tier based on orders/spending |
| `getDiscountRate()` | `BigDecimal subTotal` | BigDecimal | Returns applicable discount rate (0-15%) |

**Business Rules**:
- **Tier Calculation**:
  - PLATINUM: ≥20 orders OR ≥15,000 DH spent
  - GOLD: ≥10 orders OR ≥5,000 DH spent
  - SILVER: ≥3 orders OR ≥1,000 DH spent
  - BRONZE: Default

- **Discount Rates**:
  - SILVER: 5% on orders ≥500 DH
  - GOLD: 10% on orders ≥800 DH
  - PLATINUM: 15% on orders ≥1,200 DH

---

#### 3. **Admin** (extends User)
**Location**: `com.jartiste.smartshop.domain.entity.Admin`

**Purpose**: Administrator accounts with full system access.

**Attributes**: Inherits from User, no additional fields.

---

#### 4. **Product**
**Location**: `com.jartiste.smartshop.domain.entity.Product`

**Purpose**: Represents items available for purchase.

**Attributes**:
- `id`: Product ID
- `name`: Product name
- `price`: Unit price
- `stock`: Available quantity
- `deleted`: Soft delete flag
- `createdAt`, `updatedAt`: Audit timestamps

**Methods**:

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| `decreaseStock()` | `int quantity` | void | Reduces stock, throws exception if insufficient |
| `hasAvailableStock()` | `int quantity` | boolean | Checks if quantity is available |
| `increaseStock()` | `int quantity` | void | Adds quantity back to stock |

**Business Rules**:
- Cannot decrease stock below zero
- Deleted products are hidden but not removed from database
- Stock changes are atomic

---

#### 5. **Order**
**Location**: `com.jartiste.smartshop.domain.entity.Order`

**Purpose**: Represents customer purchase transactions.

**Attributes**:
- `id`: Order ID
- `orderStatus`: Current status (PENDING, CONFIRMED, REJECTED, CANCELED)
- `promoCode`: Optional promotional code
- `subTotal`: Sum of line items before discounts
- `discountAmount`: Total discount applied
- `taxAmount`: VAT (20%)
- `totalAmount`: Final amount due
- `remainingAmount`: Unpaid balance
- `client`: Associated client
- `itemList`: List of order items
- `payments`: List of payments made
- `createdAt`, `updatedAt`: Audit timestamps

**Status Flow**:
```
PENDING → CONFIRMED (when fully paid)
PENDING → CANCELED (manual cancellation)
PENDING → REJECTED (insufficient stock)
```

---

#### 6. **OrderItem**
**Location**: `com.jartiste.smartshop.domain.entity.OrderItem`

**Purpose**: Line item in an order linking products and quantities.

**Attributes**:
- `id`: Item ID
- `quantity`: Number of units
- `unitPrice`: Price per unit at time of order
- `product`: Associated product
- `order`: Parent order
- `createdAt`, `updatedAt`: Audit timestamps

---

#### 7. **Payment**
**Location**: `com.jartiste.smartshop.domain.entity.Payment`

**Purpose**: Records payment transactions for orders.

**Attributes**:
- `id`: Payment ID
- `amount`: Payment amount
- `paymentMethod`: Method (ESPECES, CHEQUE, VIREMENT)
- `paymentStatus`: Status (EN_ATTENTE, ENCAISSE, REJETE)
- `reference`: Transaction reference
- `bankName`: Bank name (for CHEQUE/VIREMENT)
- `dueDate`: Check due date (for CHEQUE)
- `clearedDate`: When payment was cleared
- `order`: Associated order
- `createdAt`, `updatedAt`: Audit timestamps

---

### Enumerations

#### **OrderStatus**
- `PENDING`: Awaiting payment
- `CONFIRMED`: Fully paid and confirmed
- `REJECTED`: Rejected due to stock issues
- `CANCELED`: Manually canceled

#### **PaymentMethod**
- `ESPECES`: Cash payment
- `CHEQUE`: Check payment
- `VIREMENT`: Bank transfer

#### **PaymentStatus**
- `EN_ATTENTE`: Pending clearance
- `ENCAISSE`: Cleared/collected
- `REJETE`: Rejected

#### **CustomerTier**
- `BRONZE`: Default tier
- `SLIVER`: 3+ orders or 1,000+ DH
- `GOLD`: 10+ orders or 5,000+ DH
- `PLATINUM`: 20+ orders or 15,000+ DH

#### **UserRole**
- `ADMIN`: Full system access
- `CLIENT`: Customer access

---

### Domain Services

#### **OrderDomainService**
**Location**: `com.jartiste.smartshop.domain.service.OrderDomainService`

**Purpose**: Encapsulates complex order creation and calculation logic.

**Methods**:

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| `initializeOrder()` | `Client client, String promoCode` | Order | Creates new order in PENDING status |
| `processOrderItem()` | `Order order, List<OrderItem> items` | void | Validates stock, decreases inventory, calculates subtotal |
| `calculateFinalAmounts()` | `Order order` | void | Applies discounts, calculates tax and total |

**Calculation Logic**:
1. Calculate subtotal from line items
2. Apply customer tier discount
3. Apply promo code discount (5% extra if valid)
4. Calculate net amount (subtotal - discounts)
5. Apply 20% VAT on net amount
6. Set total and remaining amounts

**Promo Code Pattern**: `PROMO-[A-Z0-9]{4}` (e.g., PROMO-ABCD)

---

### Exceptions

#### **BusinessLogicViolation**
Thrown when business rules are violated (e.g., cash payment > 20,000 DH)

#### **ResourceNotFound**
Thrown when requested entity doesn't exist

#### **UnAuthorizedException**
Thrown when user is not authenticated

#### **ForbiddenException**
Thrown when user lacks required permissions

#### **UsernameOrPasswordIncorrect**
Thrown when login credentials are invalid

#### **ValidationException**
Thrown for input validation errors

---

## Application Layer

The application layer orchestrates domain logic and provides use cases.

### Services

#### 1. **ClientService**
**Location**: `com.jartiste.smartshop.application.service.ClientService`

**Purpose**: Manages client CRUD operations and authentication.

**Methods**:

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| `createClient()` | `ClientRequest` | ClientResponse | Creates new client account with hashed password |
| `getClientById()` | `Long id` | ClientResponse | Retrieves client by ID |
| `getAllClients()` | `Pageable` | Page<ClientResponse> | Paginated list of all clients |
| `updateClient()` | `Long id, ClientRequest` | ClientResponse | Updates client information |
| `deleteClient()` | `Long id` | void | Hard deletes client |

**Business Rules**:
- Username must be unique
- Password is automatically hashed before storage
- Throws `BusinessLogicViolation` if username exists
- Throws `ResourceNotFound` if client not found

---

#### 2. **ProductService**
**Location**: `com.jartiste.smartshop.application.service.ProductService`

**Purpose**: Manages product catalog with soft delete support.

**Methods**:

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| `createProduct()` | `ProductRequest` | ProductResponse | Creates new product |
| `getAllActiveProduct()` | `Pageable` | Page<ProductResponse> | Returns non-deleted products |
| `getProductById()` | `Long id` | ProductResponse | Retrieves active product by ID |
| `updateProduct()` | `Long id, ProductRequest` | ProductResponse | Updates product details |
| `deleteProduct()` | `Long id` | void | Soft deletes product (sets deleted=true) |

**Business Rules**:
- Only active (non-deleted) products are returned
- Delete is soft (sets flag, doesn't remove from DB)

---

#### 3. **OrderService**
**Location**: `com.jartiste.smartshop.application.service.OrderService`

**Purpose**: Handles order creation, validation, and cancellation.

**Methods**:

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| `createOrder()` | `OrderRequest` | OrderResponse | Creates new order with items |
| `getOrderById()` | `Long id` | OrderResponse | Retrieves order details |
| `getOrderByClient()` | `Long clientId, Pageable` | Page<OrderResponse> | Client's order history |
| `validateOrder()` | `Long orderId` | OrderResponse | Confirms fully paid order |
| `cancelOrder()` | `Long orderId` | void | Cancels pending order and restores stock |

**Order Creation Flow**:
1. Validate client exists
2. Update client's first/last order dates
3. Initialize order with promo code
4. Validate product availability
5. Create order items and decrease stock
6. Calculate subtotal, discounts, tax, and total
7. Save order and update client

**Validation Rules**:
- Only PENDING orders can be validated
- Order must be fully paid (remainingAmount = 0)
- Updates client statistics on confirmation

**Cancellation Rules**:
- Only PENDING orders can be canceled
- Stock is restored for all order items
- Order status changes to CANCELED

---

#### 4. **PaymentService**
**Location**: `com.jartiste.smartshop.application.service.PaymentService`

**Purpose**: Processes payments for orders with multiple payment methods.

**Methods**:

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| `addPayment()` | `Long orderId, PaymentRequest` | PaymentResponse | Adds payment to order |
| `validatePaymentMethodRules()` | `PaymentRequest` | void | Validates payment method requirements |
| `generateReference()` | `PaymentRequest` | String | Generates payment reference |
| `determineInitialStatus()` | `PaymentMethod` | PaymentStatus | Sets initial payment status |
| `buildPaymentResponse()` | `Payment` | PaymentResponse | Converts entity to response DTO |

**Payment Method Rules**:

**ESPECES (Cash)**:
- Maximum: 20,000 DH
- Auto-generates reference: `ESP-{timestamp}`
- Status: ENCAISSE (immediately cleared)
- Cleared date: Set to now

**CHEQUE (Check)**:
- Required: reference, bankName, dueDate
- Status: EN_ATTENTE (pending clearance)
- Cleared date: null (until cleared)

**VIREMENT (Bank Transfer)**:
- Required: reference, bankName
- Status: EN_ATTENTE (pending clearance)
- Cleared date: null (until cleared)

**Payment Processing Flow**:
1. Validate order exists and is not canceled/rejected
2. Validate payment amount ≤ remaining amount
3. Validate payment method requirements
4. Generate reference if needed
5. Determine initial status
6. Create payment record
7. Reduce order's remaining amount
8. Auto-confirm order if fully paid
9. Update client statistics if confirmed

**Auto-Confirmation**:
- If `remainingAmount == 0` and status is PENDING
- Order status → CONFIRMED
- Client statistics updated (total orders, total spent, tier)

---

#### 5. **AuthService**
**Location**: `com.jartiste.smartshop.application.service.AuthService`

**Purpose**: Handles user authentication and session management.

**Methods**:

| Method | Parameters | Return | Description |
|--------|-----------|--------|-------------|
| `login()` | `LoginRequest, HttpSession` | AuthResponse | Authenticates user and creates session |
| `logout()` | `HttpSession` | void | Invalidates session |

**Login Flow**:
1. Find user by username
2. Verify password using BCrypt
3. Create session with USER_ID and USER_ROLE
4. Return user details including tier (for clients)

**Session Attributes**:
- `USER_ID`: Logged-in user's ID
- `USER_ROLE`: User's role (ADMIN or CLIENT)

---

### Mappers

Mappers convert between domain entities and DTOs using MapStruct.

#### **ClientMapper**
- `toEntity()`: ClientRequest → Client
- `toResponse()`: Client → ClientResponse
- `updateEntityFromDto()`: Updates client from request

#### **ProductMapper**
- `toEntity()`: ProductRequest → Product
- `toResponse()`: Product → ProductResponse
- `updateProductFromDto()`: Updates product from request

#### **OrderMapper**
- `toResponse()`: Order → OrderResponse
- Maps nested order items and payments

---

## Presentation Layer

The presentation layer handles HTTP requests and responses.

### Controllers

#### 1. **AuthController**
**Location**: `com.jartiste.smartshop.presentation.controller.AuthController`

**Endpoints**:

| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/logout` | User logout | Yes |

---

#### 2. **ClientController**
**Location**: `com.jartiste.smartshop.presentation.controller.ClientController`

**Endpoints**:

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| POST | `/api/clients` | ADMIN | Create client |
| GET | `/api/clients` | ADMIN | List all clients (paginated) |
| GET | `/api/clients/{id}` | ADMIN, CLIENT* | Get client details |
| PUT | `/api/clients/{id}` | ADMIN, CLIENT* | Update client |
| DELETE | `/api/clients/{id}` | ADMIN | Delete client |

*Clients can only access their own data

**Access Control**: 
- `validateAccess()` method ensures clients can only access their own records
- Admins have unrestricted access

---

#### 3. **ProductController**
**Location**: `com.jartiste.smartshop.presentation.controller.ProductController`

**Endpoints**:

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | `/api/products` | ADMIN, CLIENT | List products (paginated) |
| GET | `/api/products/{id}` | ADMIN, CLIENT | Get product details |
| POST | `/api/products` | ADMIN | Create product |
| PUT | `/api/products/{id}` | ADMIN | Update product |
| DELETE | `/api/products/{id}` | ADMIN | Delete product (soft) |

**Query Parameters**:
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)

---

#### 4. **OrderController**
**Location**: `com.jartiste.smartshop.presentation.controller.OrderController`

**Endpoints**:

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| POST | `/api/orders` | ADMIN, CLIENT* | Create order |
| GET | `/api/orders/{id}` | ADMIN, CLIENT* | Get order details |
| GET | `/api/orders/client/{clientId}` | ADMIN, CLIENT* | Get client's orders |

*Clients can only access their own orders

**Access Control**:
- `validateClientAccess()` ensures clients only see their own orders
- Admins can access all orders

---

#### 5. **OrderActionController**
**Location**: `com.jartiste.smartshop.presentation.controller.OrderActionController`

**Purpose**: Handles order state transitions and payments.

**Endpoints**:

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| POST | `/api/orders/{orderId}/validate` | ADMIN | Validate/confirm order |
| POST | `/api/orders/{orderId}/cancel` | ADMIN | Cancel order |
| POST | `/api/orders/{orderId}/payments` | ADMIN, CLIENT* | Add payment |

*Clients can only pay for their own orders

---

### DTOs (Data Transfer Objects)

#### Request DTOs

**ClientRequest**:
```java
record ClientRequest(String username, String password, String nom)
```

**ProductRequest**:
```java
record ProductRequest(String name, BigDecimal price, Integer stock)
```

**OrderRequest**:
```java
record OrderRequest(Long ClientId, String promoCode, List<OrderItemRequest> items)
```

**OrderItemRequest**:
```java
record OrderItemRequest(Long productId, Integer quantity)
```

**PaymentRequest**:
```java
record PaymentRequest(
    BigDecimal amount,
    PaymentMethod paymentMethod,
    String reference,
    String bankName,
    LocalDate dueDate
)
```

**LoginRequest**:
```java
record LoginRequest(String username, String password)
```

#### Response DTOs

**ClientResponse**: Contains client details including tier and statistics

**ProductResponse**: Contains product information

**OrderResponse**: Contains order details with items and payments

**OrderItemResponse**: Contains item details with product info

**PaymentResponse**: Contains payment details and status

**AuthResponse**: Contains user info after login (id, username, role, tier, message)

---

### Exception Handling

#### **GlobalExceptionHandler**
**Location**: `com.jartiste.smartshop.presentation.advice.GlobalExceptionHandler`

**Purpose**: Centralized exception handling using Spring's `@ControllerAdvice`.

**Handled Exceptions**:

| Exception | HTTP Status | Description |
|-----------|------------|-------------|
| `BusinessLogicViolation` | 422 UNPROCESSABLE_ENTITY | Business rule violation |
| `ResourceNotFound` | 404 NOT_FOUND | Entity not found |
| `UnAuthorizedException` | 401 UNAUTHORIZED | Not authenticated |
| `ForbiddenException` | 403 FORBIDDEN | Insufficient permissions |
| `UsernameOrPasswordIncorrect` | 401 UNAUTHORIZED | Invalid credentials |
| `ValidationException` | 400 BAD_REQUEST | Input validation error |
| `Exception` | 500 INTERNAL_SERVER_ERROR | Unexpected errors |

**Response Format**: RFC 7807 ProblemDetail with timestamp and path

---

### Annotations

#### **@RequireRole**
**Location**: `com.jartiste.smartshop.presentation.annotation.RequireRole`

**Purpose**: Declarative role-based access control.

**Usage**:
```java
@RequireRole(UserRole.ADMIN)
public ResponseEntity<?> adminOnlyEndpoint() { ... }

@RequireRole({UserRole.ADMIN, UserRole.CLIENT})
public ResponseEntity<?> multiRoleEndpoint() { ... }
```

---

## Infrastructure Layer

### Configuration

#### **AuthInterceptor**
**Location**: `com.jartiste.smartshop.infrastructure.config.AuthInterceptor`

**Purpose**: Intercepts requests to enforce role-based access control.

**Flow**:
1. Check if handler has `@RequireRole` annotation
2. Verify session exists with USER_ROLE attribute
3. Validate user's role matches required role(s)
4. Throw exception if unauthorized/forbidden

**Exceptions**:
- `UnAuthorizedException`: No active session
- `ForbiddenException`: Role doesn't match

---

#### **WebConfig**
**Location**: `com.jartiste.smartshop.infrastructure.config.WebConfig`

**Purpose**: Registers interceptors and web configurations.

---

#### **PasswordUtil**
**Location**: `com.jartiste.smartshop.infrastructure.util.PasswordUtil`

**Purpose**: Password hashing and verification using BCrypt.

**Methods**:
- `hashPassword(String plainPassword)`: Hashes password
- `checkPassword(String plainPassword, String hashedPassword)`: Verifies password

---

#### **DataInitializer**
**Location**: `com.jartiste.smartshop.infrastructure.seeder.DataInitializer`

**Purpose**: Seeds initial data (admin accounts, sample products).

**Run**: On application startup (`@PostConstruct`)

---

### Repositories

All repositories extend `JpaRepository` and are interfaces.

#### **UserRepository**
- `findByUsername(String username)`: Find user by username
- `existsByUsername(String username)`: Check if username exists

#### **ClientRepository**
- Standard CRUD operations from JpaRepository

#### **ProductRepository**
- `findAllByDeletedFalse(Pageable)`: Get active products
- `findByIdAndDeletedFalse(Long id)`: Get active product by ID

#### **OrderRepository**
- `findByClient_Id(Long clientId, Pageable)`: Get client's orders

#### **PaymentRepository**
- Standard CRUD operations from JpaRepository

---

## Testing

### Unit Tests

All tests use **JUnit 5** with **Mockito** for mocking dependencies.

#### **ClientServiceTest**
**Location**: `com.jartiste.smartshop.application.service.ClientServiceTest`

**Tests**:
- Create client with valid data
- Prevent duplicate usernames
- Retrieve client by ID
- Update client information
- Delete client

---

#### **ProductServiceTest**
**Location**: `com.jartiste.smartshop.application.service.ProductServiceTest`

**Tests**:
- Create product
- List active products only
- Update product
- Soft delete product
- Prevent retrieving deleted products

---

#### **OrderServiceTest**
**Location**: `com.jartiste.smartshop.application.service.OrderServiceTest`

**Tests**:
- Create order with valid items
- Reject order with insufficient stock
- Calculate discounts correctly (tier + promo)
- Calculate tax (20% VAT)
- Validate fully paid order
- Cancel order and restore stock
- Prevent validating unpaid orders
- Prevent canceling non-pending orders

---

#### **PaymentServiceTest**
**Location**: `com.jartiste.smartshop.application.service.PaymentServiceTest`

**Test Cases**:

| Test | Description |
|------|-------------|
| `shouldAddValidPayment()` | Adds partial payment, reduces remaining amount |
| `shouldFailForLargeCashPayment()` | Rejects cash payment > 20,000 DH |
| `shouldFailIfAmountExceedRemaining()` | Rejects payment > remaining amount |
| `shouldAutoConfirmOrderWhenFullyPaid()` | Auto-confirms order when fully paid |
| `shouldFailIfChequePaymentMissingFields()` | Validates check requires reference, bank, date |
| `shouldFailIfVirementPaymentMissingFields()` | Validates transfer requires reference, bank |

---

#### **OrderDomainServiceTest**
**Location**: `com.jartiste.smartshop.domain.service.OrderDomainServiceTest`

**Tests**:
- Order initialization
- Stock validation and deduction
- Discount calculation (tier-based)
- Promo code application
- Tax calculation
- Total amount calculation

---

#### **Entity Tests**

**ClientTest**:
- Tier recalculation based on orders/spending
- Discount rate calculation by tier
- Statistics update

**ProductTest**:
- Stock increase/decrease
- Insufficient stock exception
- Stock availability check

---

## Business Rules Summary

### Order Processing
1. **Order Creation**:
   - Validates all products exist and have sufficient stock
   - Decreases stock atomically during order creation
   - Auto-rejects if any item has insufficient stock
   - Calculates: subtotal → discounts → tax → total

2. **Discount Hierarchy**:
   - Customer tier discount (0-15%)
   - Promo code discount (+5% if valid)
   - Both can be combined

3. **Tax Calculation**:
   - 20% VAT applied to net amount (after discounts)

4. **Order Confirmation**:
   - Requires status = PENDING
   - Requires full payment (remainingAmount = 0)
   - Updates client statistics (total orders, spent, tier)
   - Auto-confirms when last payment completes order

5. **Order Cancellation**:
   - Only PENDING orders can be canceled
   - Restores product stock for all items
   - Sets status to CANCELED

### Payment Processing
1. **Cash Payments**:
   - Maximum: 20,000 DH
   - Immediately cleared (ENCAISSE)
   - Auto-generates reference

2. **Check Payments**:
   - Requires: reference, bank name, due date
   - Status: EN_ATTENTE (pending)
   - Manual clearance required

3. **Bank Transfers**:
   - Requires: reference, bank name
   - Status: EN_ATTENTE (pending)
   - Manual clearance required

4. **Partial Payments**:
   - Allowed (payment < remaining amount)
   - Multiple payments can be made
   - Auto-confirms when total payments = order total

### Client Tier System
1. **Tier Progression**:
   - Automatic recalculation after each confirmed order
   - Based on orders count OR total spent (whichever qualifies)

2. **Tier Benefits**:
   - Higher tiers get better discount rates
   - Discounts require minimum order amount
   - Displayed in client profile

### Stock Management
1. **Inventory Tracking**:
   - Stock decreased on order creation
   - Stock restored on order cancellation
   - Cannot order if insufficient stock

2. **Product Deletion**:
   - Soft delete (sets deleted flag)
   - Historical orders still reference deleted products
   - Deleted products hidden from catalog

### Authentication & Authorization
1. **Session-Based Auth**:
   - Login creates session with USER_ID and USER_ROLE
   - Logout invalidates session
   - No JWT/tokens (session-based)

2. **Role-Based Access**:
   - ADMIN: Full system access
   - CLIENT: Can only access own data (orders, profile)
   - Enforced via `@RequireRole` annotation + interceptor

3. **Data Isolation**:
   - Clients cannot view other clients' data
   - Controllers validate access for client-specific endpoints
   - Admins bypass access checks

---

## API Examples

### Create Client
```http
POST /api/clients
Authorization: ADMIN
Content-Type: application/json

{
  "username": "john.doe",
  "password": "SecurePass123",
  "nom": "John Doe"
}
```

### Create Order
```http
POST /api/orders
Authorization: CLIENT or ADMIN
Content-Type: application/json

{
  "ClientId": 1,
  "promoCode": "PROMO-ABCD",
  "items": [
    {
      "productId": 5,
      "quantity": 2
    },
    {
      "productId": 8,
      "quantity": 1
    }
  ]
}
```

### Add Payment
```http
POST /api/orders/1/payments
Authorization: CLIENT (owner) or ADMIN
Content-Type: application/json

{
  "amount": 5000.00,
  "paymentMethod": "CHEQUE",
  "reference": "CHQ-123456",
  "bankName": "Bank Al-Maghrib",
  "dueDate": "2025-01-15"
}
```

---

## Key Workflows

### 1. Complete Order Flow
```
Client Login
    ↓
Browse Products
    ↓
Create Order (stock checked, decreased)
    ↓
Add Payment (partial or full)
    ↓
[If fully paid] → Auto-Confirm → Update Client Stats
    ↓
Order Complete
```

### 2. Payment Flow
```
Order Created (remainingAmount = totalAmount)
    ↓
Payment 1 Added (remainingAmount -= payment1)
    ↓
[If remainingAmount > 0] → Payment 2 Added
    ↓
[If remainingAmount == 0] → Auto-Confirm Order
    ↓
Update Client: totalOrders++, totalSpent += amount, recalculateTier()
```

### 3. Cancellation Flow
```
Order in PENDING status
    ↓
Admin/Client Cancels Order
    ↓
Restore Stock for All Items
    ↓
Set Status = CANCELED
    ↓
(Payments are kept for audit trail)
```

---

## Testing Coverage

Run tests with coverage:
```bash
./mvnw clean test jacoco:report
```

View coverage report:
```
target/site/jacoco/index.html
```

**Current Coverage** (as per test reports):
- Service layer: ~90%+
- Domain entities: ~85%+
- Controllers: Integration tests recommended

---

## Configuration Files

### application.yaml
Main configuration for database, JPA, server settings.

### application-dev.yml
Development environment overrides.

### application-qa.yml
QA environment overrides.

### bootstrap.yml
Pre-bootstrap configuration (e.g., config server, vault).

---

## Build & Run

### Build
```bash
./mvnw clean package
```

### Run
```bash
java -jar target/smartshop-0.0.1-SNAPSHOT.jar
```

### Run with Profile
```bash
java -jar target/smartshop-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Docker Build
```bash
docker build -t smartshop:latest .
```

---

## Conclusion

This guide covers the complete SmartShop application architecture, business logic, and implementation details. Key takeaways:

- **Clean Architecture**: Clear separation of concerns across layers
- **Domain-Driven Design**: Rich domain model with business logic in entities
- **Security**: Role-based access control with session management
- **Testing**: Comprehensive unit tests for services and domain logic
- **Business Rules**: Well-defined rules for orders, payments, and customer tiers
- **API Design**: RESTful endpoints with proper HTTP semantics

For questions or contributions, please refer to the codebase or contact the development team.

---

**Generated**: December 2025  
**Version**: 1.0  
**Application**: SmartShop E-Commerce Platform

