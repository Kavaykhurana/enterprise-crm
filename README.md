# Enterprise CRM Backend

An enterprise-grade Customer Relationship Management (CRM) backend service built on modern software architecture principles (**Clean Architecture, Domain-Driven Design Lite, SOLID**) designed to showcase production-ready engineering patterns.

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square)](https://www.oracle.com/java/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring_Boot-3.3.1-brightgreen.svg?style=flat-square)](https://spring.io/projects/spring-boot)
[![Spring Security 6](https://img.shields.io/badge/Spring_Security-6-blue.svg?style=flat-square)](https://spring.io/projects/spring-security)
[![Database](https://img.shields.io/badge/PostgreSQL-16-blue.svg?style=flat-square)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

---

## 🌟 Key Architecture & Enterprise Patterns

### 1. time-ordered UUID v7 Identifiers
Unlike standard random UUID v4, this system implements **UUID v7** for all primary keys. It combines a 48-bit millisecond timestamp with random bits, ensuring chronological sorting. This drastically reduces PostgreSQL B-tree index fragmentation and improves query insert performance while maintaining global uniqueness.

### 2. Double-Token Auth & Refresh Token Rotation (RTR)
*   **Access Token**: Short-lived (15 min) JWT containing user attributes, roles, and a `tokenVersion`.
*   **Refresh Token**: Long-lived (7 days) random string stored securely as a **SHA-256 hash** in the database.
*   **Rotation (RTR)**: Every refresh request invalidates the current token and issues a new pair. If a revoked refresh token is reused, the system automatically flags it as a breach, increments the user's `tokenVersion` to immediately invalidate all existing access tokens, and deletes all user sessions.

### 3. Token Versioning & Instant Revocation
We maintain a `token_version` column on the `User` aggregate. This version is signed inside the JWT. If a user changes their password, changes roles, gets deactivated, or signs out, the version is incremented. All previously issued JWTs are rejected immediately by filters without needing a persistent blacklist store.

### 4. Brute-Force Lockout Protection
Protects authentication endpoints from credential stuffing:
*   On 5 consecutive failed login attempts, the account is locked (`account_locked = true`).
*   Lockout expiration is set for **15 minutes** (`account_locked_until`).
*   Subsequent attempts during lockout return a standard locked error message.

### 5. Decoupled Spring Event Auditing
Write services publish Spring `ApplicationEvent` subclasses. Asynchronous `@TransactionalEventListener` classes handle these events only **after** the transaction commits successfully (`TransactionPhase.AFTER_COMMIT`). This ensures logs are decoupled from core business flows and never written for rolled-back operations.

---

## 🛠️ Technology Stack
*   **Language**: Java 21 (LTS)
*   **Framework**: Spring Boot 3.3.1 & Spring Security 6
*   **ORM**: Hibernate 6 / Spring Data JPA
*   **Mapper**: MapStruct 1.5.5 (Compile-time code generation)
*   **Rate Limiting**: Bucket4j (Token-bucket per IP filter)
*   **Database**: PostgreSQL (Production) / H2 (In-memory Test)
*   **Testing**: JUnit 5, MockMvc, Mockito
*   **Linters**: Spotless (Google Java Style), Checkstyle, SpotBugs

---

## 📂 Package Layout (Feature-First DDD-Lite)
```
com.enterprise.crm.v1
├── auth/          # Registration, JWT validation, refresh, lockout rules
├── user/          # User management, session tracking logs
├── customer/      # Customer aggregate, contacts, addresses
├── lead/          # Lead state machine, conversion service
├── opportunity/   # Revenue and opportunity sales pipeline stage tracking
├── task/          # Polymorphic task management, comments, recurrence engine
├── common/        # Shared base entities, filters, exceptions, UUIDv7 generator
```

---

## 🔒 Security Configuration
Our REST API is completely stateless and protects all endpoints. 
*   **CORS**: Non-wildcard whitelist origin verification.
*   **CSRF**: Disabled since stateless authorization headers are used.
*   **XSS Protection**: Secure headers included:
    *   `Content-Security-Policy`: frame-ancestors 'none'
    *   `X-Frame-Options`: DENY
    *   `X-Content-Type-Options`: nosniff
    *   `Permissions-Policy`: restrictive geo/camera/microphone access
*   **Cache-Control**: `no-store` headers configured on authentication endpoints to prevent caching credentials.

---

## 📊 Database ER Model

```
   +-------------------+          +--------------------+
   |       USERS       | <------  |   REFRESH_TOKENS   |
   | (UUIDv7, PK)      |          | (SHA-256 Hashed)   |
   +-------------------+          +--------------------+
             |
             |
             v
   +-------------------+
   |   SESSION_LOGS    |
   | (Device, IP, UA)  |
   +-------------------+
```

---

## 🚀 How to Run Locally

### Prerequisites
*   JDK 21 or JDK 25 installed.
*   Maven 3.9+ installed.
*   Local PostgreSQL instance running on port `5432` with a database named `crm_db`.

### 1. Clone & Configure Properties
Configure the database user and password via environment variables or replace them in `src/main/resources/application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/crm_db
    username: postgres
    password: password
```

### 2. Build and Test
Run compilation and verify using the H2 in-memory test suite:
```bash
mvn clean test
```

### 3. Run Application
```bash
mvn spring-boot:run
```
The application will boot up on `http://localhost:8080`.

---

## 📈 REST API Documentation

### Authentication Routes (`/api/v1/auth`)

| Endpoint | Method | Access | Description |
| :--- | :--- | :--- | :--- |
| `/register` | `POST` | Public | Registers a new user (admin/sales manager/sales executive). |
| `/login` | `POST` | Public | Validates credentials, creates JWT pair, updates session logs. |
| `/refresh` | `POST` | Public | Performs Refresh Token Rotation (RTR) returning new JWT pair. |
| `/logout` | `POST` | Authed | Invalidates active refresh token in the database. |

#### Sample Login Request JSON
```json
{
  "email": "sales.rep@example.com",
  "password": "StrongPassword123!"
}
```

#### Sample Successful API Response Envelope
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsIn...",
    "refreshToken": "70929283-f38b-4b2a...",
    "userId": "0190c1f4-3d04-7c30-9b36-bf4ad865768e",
    "email": "sales.rep@example.com",
    "role": "SALES_EXECUTIVE"
  },
  "timestamp": "2026-07-18T14:04:12",
  "traceId": "0190c1f4-3d04-7c30-9b36-bf4ad865768e"
}
```
