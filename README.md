# Enterprise CRM Backend

An enterprise-grade Customer Relationship Management (CRM) backend service built on modern software architecture principles (**Clean Architecture, Domain-Driven Design Lite, SOLID**) designed to showcase production-ready engineering patterns.

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square)](https://www.oracle.com/java/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring_Boot-3.3.1-brightgreen.svg?style=flat-square)](https://spring.io/projects/spring-boot)
[![Spring Security 6](https://img.shields.io/badge/Spring_Security-6-blue.svg?style=flat-square)](https://spring.io/projects/spring-security)
[![React](https://img.shields.io/badge/React-18-61DAFB.svg?style=flat-square)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-5-646CFF.svg?style=flat-square)](https://vitejs.dev/)
[![Database](https://img.shields.io/badge/PostgreSQL-16-blue.svg?style=flat-square)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

---

## 🌟 Key Architecture & Enterprise Patterns

### 1. time-ordered UUID v7 Identifiers
Unlike standard random UUID v4, this system implements **UUID v7** for all primary keys. It combines a 48-bit millisecond timestamp with random bits, ensuring chronological sorting. This drastically reduces PostgreSQL B-tree index fragmentation and improves query insert performance while maintaining global uniqueness.

### 2. Double-Token Auth & Refresh Token Rotation (RTR)
*   **Access Token**: Short-lived (15 min) JWT containing user attributes, roles, and a `tokenVersion`.
*   **Refresh Token**: Long-lived (7 days) random string stored securely as a **SHA-256 hash** in the database.
*   **Rotation (RTR)**: Every refresh request invalidates the current token and issues a new pair. If a revoked refresh token is reused, the system flags it as a breach, increments the user's `tokenVersion` to immediately invalidate all existing access tokens, and deletes all user sessions.

### 3. Token Versioning & Instant Revocation
We maintain a `token_version` column on the `User` aggregate. This version is signed inside the JWT. If a user changes their password, changes roles, gets deactivated, or signs out, the version is incremented. All previously issued JWTs are rejected immediately by filters without needing a persistent blacklist store.

### 4. Brute-Force Lockout Protection
Protects authentication endpoints from credential stuffing:
*   On 5 consecutive failed login attempts, the account is locked (`account_locked = true`).
*   Lockout expiration is set for **15 minutes** (`account_locked_until`).
*   Subsequent attempts during lockout return a standard locked error message.

### 5. Polymorphic Task & Recurrence Engine
*   **Polymorphic Links**: Tasks can bind dynamically to different aggregates (`CUSTOMER`, `LEAD`) using a soft polymorphism reference verified at the service layer.
*   **Recurrence Engine**: Supports automatic task spawning (e.g. `DAILY`, `WEEKLY`, `MONTHLY`). When a recurring task is completed (`status = COMPLETED`), the system automatically schedules the next instance with an updated due date.

### 6. Decoupled Event-Driven Timeline (JSONB Logs)
Core services publish Spring `ApplicationEvent` subclasses. Asynchronous `@TransactionalEventListener` classes capture these events **after** the transaction commits successfully (`TransactionPhase.AFTER_COMMIT`). This ensures logs are decoupled from core business flows and never written for rolled-back operations, persisting a historical timeline in PostgreSQL.

### 7. RBAC Personalized KPI Dashboards
*   **Personalization**: When a `SALES_EXECUTIVE` fetches dashboard metrics, calculations (win rates, conversion rates, pipeline weighted value, overdue tasks count) are restricted only to records they own.
*   **Global Access**: `SALES_MANAGER` and `ADMIN` users receive global aggregates.

---

## 🛠️ Technology Stack
### Backend
*   **Language**: Java 21 (LTS)
*   **Framework**: Spring Boot 3.3.1 & Spring Security 6
*   **ORM**: Hibernate 6 / Spring Data JPA
*   **Mapper**: MapStruct 1.5.5 (Compile-time code generation)
*   **Rate Limiting**: Bucket4j (Token-bucket per IP filter)
*   **Database**: PostgreSQL (Production) / H2 (In-memory Test)
*   **Testing**: JUnit 5, MockMvc, Mockito
*   **Linters**: Spotless (Google Java Style), Checkstyle, SpotBugs

### Frontend
*   **Framework**: React 18 with Vite 5
*   **Routing**: React Router v6 (lazy-loaded routes)
*   **Styling**: Tailwind CSS 3
*   **Auth**: JWT access + refresh token management via Axios interceptors
*   **State**: React Context (AuthContext), custom hooks

---

## 📂 Package Layout (Feature-First DDD-Lite)
```
com.enterprise.crm.v1
├── auth/          # Registration, JWT validation, refresh, lockout rules
├── user/          # User management, session tracking logs
├── customer/      # Customer aggregate, contacts, addresses, specification search
├── lead/          # Lead state machine, conversion service
├── opportunity/   # Revenue and opportunity sales pipeline stage tracking
├── task/          # Polymorphic task management, comments, recurrence engine
├── activity/      # Decoupled transaction audit logs
├── dashboard/     # Role-based aggregated metrics controller
├── common/        # Shared base entities, filters, exceptions, UUIDv7 generator
```

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
   +-------------------+          +--------------------+
   |   SESSION_LOGS    |          |    LEAD / CUST     |
   | (Device, IP, UA)  |          | (Soft Deleted)     |
   +-------------------+          +--------------------+
                                             |
                                             v
                                  +--------------------+          +--------------------+
                                  |       TASKS        | <------  |   TASK_COMMENTS    |
                                  | (Polymorphic, Rec) |          | (taskId)           |
                                  +--------------------+          +--------------------+
                                             |
                                             v
                                  +--------------------+
                                  |   ACTIVITY_LOGS    |
                                  | (JSONB Audit Event)|
                                  +--------------------+
```

---

## 🌐 Production Deployment Architecture

```
                                  [ HTTPS Request ]
                                          |
                                          v
                              +-----------------------+
                              |   Vercel Edge CDN     |  (Frontend React SPA)
                              |   (https://*.vercel)  |
                              +-----------------------+
                                          |
                                          | (REST / CORS Authorized)
                                          v
                              +-----------------------+
                              |   Railway App Engine  |  (Spring Boot REST API)
                              |  (https://*.railway)  |
                              +-----------------------+
                                          |
                                          | (Internal connection)
                                          v
                              +-----------------------+
                              |  Railway PostgreSQL   |  (Postgres database)
                              +-----------------------+
```

---

## 📈 REST API Documentation

### REST API Routes Index

#### 1. Authentication (`/api/v1/auth`)
*   `POST /register`: Registers a new user.
*   `POST /login`: Validates credentials and returns JWT pair.
*   `POST /refresh`: Performs Refresh Token Rotation.
*   `POST /logout`: Invalidate active refresh token.

#### 2. Customers (`/api/v1/customers`)
*   `GET /`: Paginated search (companyName, customerStatus, tag, size, range, rep).
*   `POST /`: Creates a customer.
*   `GET /{id}`: Retrieves customer details.
*   `PUT /{id}`: Updates customer.
*   `DELETE /{id}`: Soft deletes customer.
*   `PUT /{id}/restore`: Restores soft-deleted customer (checks active tax ID conflicts).
*   `PUT /{id}/assign`: Reassigns customer owner (Admin/Manager only).

#### 3. Leads (`/api/v1/leads`)
*   `GET /`: Paginated search (status, source, email, tags, rep).
*   `POST /`: Creates a lead.
*   `PUT /{id}/status`: Validates state transition and updates status.
*   `PUT /{id}/assign`: Reassigns lead owner (Admin/Manager only).
*   `DELETE /{id}`: Soft deletes lead.
*   `POST /{id}/convert`: Executes the conversion transaction, generating Customer, primary Contact, and optional Opportunity inside a single transaction.

#### 4. Tasks (`/api/v1/tasks`)
*   `POST /`: Creates task.
*   `PUT /{id}`: Updates task (triggering recurrence engine on completion).
*   `DELETE /{id}`: Soft deletes task.
*   `POST /{id}/comments`: Adds note comment to task.
*   `GET /{id}/comments`: Retrieves comments thread.

#### 5. Dashboard (`/api/v1/dashboard`)
*   `GET /metrics`: Aggregates win rate, conversion rate, pipeline value, overdue task count, and monthly acquisition trends.

---

## 🚀 Production Deployment Guide

Follow these exact step-by-step instructions to deploy the backend and frontend to Railway and Vercel.

### 1. Backend & Database Deployment (Railway)
1.  Sign in to **[Railway.app](https://railway.app)**.
2.  Click **New Project** > **Provision PostgreSQL**. This spawns a dedicated database.
3.  Once the database is up, click **New** > **GitHub Repo** and select `enterprise-crm`.
4.  Navigate to your new Service's **Settings** tab. Under **Nixpacks Configuration**, ensure it references the root folder (`/`). Nixpacks will automatically find `nixpacks.toml` and compile the Java JAR.
5.  Go to the **Variables** tab and inject these values:
    *   `SPRING_PROFILES_ACTIVE`: `prod`
    *   `JWT_SECRET`: `[Generate a secure 512-bit random string]`
    *   `CORS_ALLOWED_ORIGINS`: `https://your-frontend-subdomain.vercel.app` (Your Vercel URL)
    *   `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` will be **automatically injected** by Railway from the PostgreSQL plugin connection block.
6.  Click **Generate Domain** under the service's settings to expose the public endpoint (e.g. `https://your-crm-backend.up.railway.app`).

### 2. Frontend Deployment (Vercel)
1.  Sign in to **[Vercel.com](https://vercel.com)**.
2.  Click **Add New** > **Project** > Import the `enterprise-crm` repository.
3.  Under **Project Settings**:
    *   **Root Directory**: Set to `frontend`.
    *   **Framework Preset**: Select **Vite**.
4.  Expand the **Environment Variables** panel and add:
    *   `VITE_API_BASE_URL`: `https://your-crm-backend.up.railway.app` (Exposed Railway Domain)
5.  Click **Deploy**. Vercel will build the SPA, bundle assets, and apply `vercel.json` routing rewrites automatically.

---

## 🖥️ Development

### Prerequisites
- Java 21 (LTS)
- Node.js 18+
- PostgreSQL 16

### Running Locally

**Backend:**
```bash
# Start PostgreSQL and create database 'enterprise_crm'
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Frontend:**
```bash
cd frontend
npm install
echo "VITE_API_BASE_URL=http://localhost:8080" > .env
npm run dev
```

The backend serves the production frontend build at `http://localhost:8080` when `SPRING_PROFILES_ACTIVE=prod`. For development, run both servers independently — the Vite dev server proxies `/api` requests to the backend via the Vite proxy configuration in `frontend/vite.config.js`.

### Security Model
*   **Read endpoints** (`GET /api/v1/leads`, `/tasks`, `/dashboard/metrics`) are publicly accessible — no auth required.
*   **Write endpoints** (`POST`, `PUT`, `DELETE`, lead conversion, status transitions) require `@PreAuthorize("isAuthenticated()")` — return **403** when unauthenticated.
*   **Auth endpoints** (`/login`, `/register`, `/refresh`) are unauthenticated by design.
*   Refresh and logout endpoints require the `refreshToken` in the request body alongside the `Authorization: Bearer <accessToken>` header.

