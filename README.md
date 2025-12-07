# 🛒 SmartShop

A sophisticated e-commerce platform built with **Spring Boot 3.5** following **Domain-Driven Design (DDD)** and **Clean Architecture** principles. SmartShop provides a robust order management system with customer loyalty tiers, multi-payment support, and role-based access control.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the Application](#running-the-application)
- [Configuration](#-configuration)
- [API Endpoints](#-api-endpoints)
- [Business Rules](#-business-rules)
- [Testing](#-testing)
- [Docker Deployment](#-docker-deployment)
- [Project Structure](#-project-structure)
- [Documentation](#-documentation)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Features

### Core Functionality
- ✅ **Product Management** - Complete CRUD operations with stock tracking
- ✅ **Order Processing** - Multi-item orders with automatic calculations
- ✅ **Payment System** - Support for multiple payment methods (Cash, Credit Card, Bank Transfer, Check)
- ✅ **Customer Loyalty Tiers** - Automatic tier upgrades (Bronze → Silver → Gold → Platinum)
- ✅ **Role-Based Access Control** - Admin and Client user roles with permission management
- ✅ **Discount Engine** - Tier-based automatic discounts (5% - 15%)

### Technical Features
- 🔐 **Security** - BCrypt password encryption
- 📊 **Pagination** - Efficient data retrieval with page support
- ✅ **Validation** - Bean validation with custom constraints
- 🔄 **MapStruct** - Type-safe object mapping
- 📝 **JPA/Hibernate** - Advanced ORM with entity relationships
- 🧪 **Test Coverage** - Unit and integration tests with JUnit 5
- 🐳 **Docker Ready** - Multi-stage Dockerfile with optimized JRE
- ☁️ **Cloud Config** - HashiCorp Vault integration for secrets management

---

## 🏗️ Architecture

SmartShop follows a **4-layer Clean Architecture** pattern ensuring separation of concerns and maintainability:

```
┌──────────────────────────────────────────────────────────┐
│                  Presentation Layer                      │
│           (Controllers, DTOs, Exception Handlers)        │
├──────────────────────────────────────────────────────────┤
│                  Application Layer                       │
│          (Services, Mappers, Orchestration)              │
├──────────────────────────────────────────────────────────┤
│                    Domain Layer                          │
│      (Entities, Value Objects, Business Logic)           │
├──────────────────────────────────────────────────────────┤
│                 Infrastructure Layer                     │
│       (Repositories, Configuration, Utilities)           │
└──────────────────────────────────────────────────────────┘
```

### Key Design Patterns
- **Domain-Driven Design (DDD)** - Rich domain models with business logic encapsulation
- **Repository Pattern** - Abstraction over data access
- **DTO Pattern** - Request/Response objects for API layer
- **Joined Table Inheritance** - User entity hierarchy (Client/Admin)
- **Service Layer Pattern** - Business orchestration and transaction management

---

## 🛠️ Tech Stack

### Core Framework
- **Java 21** - Latest LTS with modern language features
- **Spring Boot 3.5.8** - Enterprise application framework
- **Spring Data JPA** - Data persistence and repository abstraction
- **Spring Validation** - Bean validation framework

### Database
- **PostgreSQL** - Production database
- **H2** - In-memory database for development/testing

### Tools & Libraries
- **Lombok** - Boilerplate code reduction
- **MapStruct 1.6.3** - Type-safe bean mapping
- **BCrypt** - Password hashing
- **JaCoCo** - Code coverage reporting
- **Maven** - Build and dependency management

### Cloud & DevOps
- **Spring Cloud Vault** - Secrets management
- **Docker** - Containerization with optimized JRE
- **SonarQube** - Code quality analysis

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed:

- **Java 21** or higher
- **Maven 3.9+**
- **PostgreSQL 15+** (for production profile)
- **Docker** (optional, for containerized deployment)
- **HashiCorp Vault** (optional, for QA profile)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/SmartShop.git
   cd SmartShop
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run tests**
   ```bash
   mvn test
   ```

### Running the Application

#### Development Mode (H2 Database)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

**H2 Console:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** *(empty)*

#### QA Mode (PostgreSQL with Vault)
```bash
# Start Vault (if using)
docker run --cap-add=IPC_LOCK -d --name=dev-vault -p 8200:8200 vault

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=qa
```

#### Production Mode
```bash
# Configure PostgreSQL connection in application-prod.yml
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## ⚙️ Configuration

### Environment Profiles

| Profile | Database | Purpose |
|---------|----------|---------|
| `dev` | H2 (in-memory) | Development and testing |
| `qa` | PostgreSQL + Vault | Quality assurance |

### Application Properties

**`application.yaml`** - Base configuration
```yaml
spring:
  application:
    name: smartshop
  profiles:
    active: dev
```

**`application-dev.yml`** - Development settings
```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password:
```

**`bootstrap.yml`** - Vault configuration for secrets
```yaml
spring:
  cloud:
    vault:
      host: localhost
      port: 8200
      token: root
      kv:
        enabled: true
        backend: secret
```

---

## 📡 API Endpoints

### Authentication
```http
POST /api/auth/login       # User login
POST /api/auth/register    # User registration
```

### Products
```http
GET    /api/products              # List all products (paginated)
GET    /api/products/{id}         # Get product by ID
POST   /api/products              # Create product (Admin only)
PUT    /api/products/{id}         # Update product (Admin only)
DELETE /api/products/{id}         # Delete product (Admin only)
```

### Clients
```http
GET    /api/clients               # List all clients (Admin only)
GET    /api/clients/{id}          # Get client by ID
POST   /api/clients               # Create client
PUT    /api/clients/{id}          # Update client
DELETE /api/clients/{id}          # Delete client (Admin only)
```

### Orders
```http
GET    /api/orders                # List all orders
GET    /api/orders/{id}           # Get order by ID
POST   /api/orders                # Create new order
PUT    /api/orders/{id}           # Update order
DELETE /api/orders/{id}           # Cancel order

POST   /api/orders/{id}/confirm   # Confirm order
POST   /api/orders/{id}/ship      # Ship order
POST   /api/orders/{id}/deliver   # Deliver order
POST   /api/orders/{id}/cancel    # Cancel order
```

### Example Request/Response

**Create Order**
```http
POST /api/orders
Content-Type: application/json

{
  "clientId": 1,
  "items": [
    {
      "productId": 5,
      "quantity": 2
    }
  ]
}
```

**Response**
```json
{
  "id": 10,
  "clientId": 1,
  "status": "PENDING",
  "subTotal": 1000.00,
  "discount": 50.00,
  "totalAmount": 950.00,
  "orderDate": "2025-12-05T10:30:00",
  "items": [
    {
      "productId": 5,
      "productName": "Laptop",
      "quantity": 2,
      "unitPrice": 500.00,
      "subTotal": 1000.00
    }
  ]
}
```

---

## 💼 Business Rules

### Customer Loyalty Tiers

SmartShop implements an automatic tier upgrade system based on customer activity:

| Tier | Requirements | Discount Rate | Minimum Order |
|------|-------------|---------------|---------------|
| **Bronze** | Default | 0% | N/A |
| **Silver** | ≥3 orders OR ≥1,000 DH spent | 5% | ≥500 DH |
| **Gold** | ≥10 orders OR ≥5,000 DH spent | 10% | ≥800 DH |
| **Platinum** | ≥20 orders OR ≥15,000 DH spent | 15% | ≥1,200 DH |

### Order Processing Workflow

```
PENDING → CONFIRMED → SHIPPED → DELIVERED
   ↓
CANCELLED (possible at any stage)
```

### Payment Methods

- **CASH** - Cash on delivery
- **BANK_TRANSFER** - Direct bank transfer (requires reference)
- **CHECK** - Check payment (requires bank name and due date)

### Stock Management

- Products track available stock quantity
- Orders automatically reduce stock on confirmation
- Validation prevents overselling
- Products can be marked as inactive

---

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProductServiceTest

# Run with coverage report
mvn clean test jacoco:report
```

### Test Coverage

Coverage reports are generated in: `target/site/jacoco/index.html`

### Test Structure

```
src/test/java/
├── domain/
│   ├── entity/          # Entity business logic tests
│   │   ├── ClientTest.java
│   │   └── ProductTest.java
│   └── service/         # Domain service tests
│       └── OrderDomainServiceTest.java
└── application/
    └── service/         # Application service tests
        ├── ClientServiceTest.java
        ├── OrderServiceTest.java
        ├── PaymentServiceTest.java
        └── ProductServiceTest.java
```

---

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t smartshop:latest .
```

### Run Container

```bash
docker run -d \
  --name smartshop \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  smartshop:latest
```

### Multi-Stage Build

The Dockerfile uses a **3-stage build** for optimization:

1. **Build Stage** - Maven build with dependencies
2. **JRE Builder** - Custom JRE using `jlink` (reduced size)
3. **Runtime** - Minimal Alpine image with custom JRE

**Final image size:** ~150MB (vs 400MB+ with full JDK)

### Docker Compose (Example)

```yaml
version: '3.8'
services:
  smartshop:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=qa
      - VAULT_TOKEN=root
    depends_on:
      - postgres
      - vault

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: smartshop
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: secret
    ports:
      - "5432:5432"

  vault:
    image: vault:latest
    ports:
      - "8200:8200"
    environment:
      VAULT_DEV_ROOT_TOKEN_ID: root
```

---

## 📁 Project Structure

```
SmartShop/
├── src/
│   ├── main/
│   │   ├── java/com/jartiste/smartshop/
│   │   │   ├── application/           # Application Layer
│   │   │   │   ├── mapper/           # MapStruct mappers
│   │   │   │   └── service/          # Application services
│   │   │   ├── domain/               # Domain Layer
│   │   │   │   ├── entity/           # Domain entities
│   │   │   │   ├── enums/            # Enumerations
│   │   │       └── exception/        # Exception handling
│   │   │   │   ├── repository/       # Repository interfaces
│   │   │   │   └── service/          # Domain services
│   │   │   ├── infrastructure/       # Infrastructure Layer
│   │   │   │   ├── config/           # Configuration classes
│   │   │   │   └── util/             # Utility classes
│   │   │   └── presentation/         # Presentation Layer
│   │   │       ├── annotation/       # Custom annotations
│   │   │       ├── controller/       # REST controllers
│   │   │       ├── dto/              # Data Transfer Objects
│   │   │       │   ├── request/      # Request DTOs
│   │   │       │   └── response/     # Response DTOs
│   │   └── resources/
│   │       ├── application.yaml      # Main config
│   │       ├── application-dev.yml   # Dev profile
│   │       ├── application-qa.yml    # QA profile
│   │       └── bootstrap.yml         # Cloud config
│   └── test/                         # Test sources
├── target/                           # Build output
├── conception/                       # Design diagrams
├── Dockerfile                        # Docker configuration
├── pom.xml                          # Maven configuration
├── DEVELOPER_GUIDE.md               # Developer documentation
├── ENTITY_DOCUMENTATION.md          # Entity reference
└── README.md                        # This file
```

---

## 📚 Documentation

- **[Developer Guide](DEVELOPER_GUIDE.md)** - Comprehensive development guide with architecture details
- **[Class Diagram](conception/ClassDiagram-Final.jpg)** - Visual representation of domain model

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit your changes** (`git commit -m 'Add amazing feature'`)
4. **Push to the branch** (`git push origin feature/amazing-feature`)
5. **Open a Pull Request**

### Coding Standards
- Follow Java naming conventions
- Write unit tests for new features
- Maintain code coverage above 80%
- Document public APIs with JavaDoc
- Use meaningful commit messages

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

**Jartiste Team**
- Organization: [younesbousfiha](https://github.com/younesbousfiha)

---

## 🙏 Acknowledgments

- Spring Framework team for excellent documentation
- Domain-Driven Design community
- All contributors and testers

---

## 📞 Support

For support and questions:
- 📧 Email: support@jartiste.com
- 🐛 Issues: [GitHub Issues](https://github.com/yourusername/SmartShop/issues)
- 📖 Wiki: [Project Wiki](https://github.com/yourusername/SmartShop/wiki)

---

<div align="center">

**Made with ❤️ by Younes Bousfiha**

⭐ Star us on GitHub — it motivates us a lot!

</div>

